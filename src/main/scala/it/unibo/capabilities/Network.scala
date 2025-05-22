package it.unibo.capabilities

import it.unibo.capabilities.Multitier.ResourceReference
import ox.*
import ox.flow.Flow
import sttp.client4.impl.ox.ws.asSourceAndSink
import sttp.client4.ws.SyncWebSocket
import sttp.client4.ws.sync.asWebSocket
import sttp.client4.{DefaultSyncBackend, UriContext, asString, basicRequest}
import sttp.tapir.*
import sttp.tapir.server.netty.sync.OxStreams
import sttp.tapir.server.netty.sync.OxStreams.Pipe
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.ws.WebSocketFrame

import scala.collection.mutable

trait Network:
  def receiveFrom[V](from: ResourceReference)(using Ox): V
  def receiveFromAll[V](from: ResourceReference)(using Ox): Seq[V]
  def receiveFlowFrom[V](from: ResourceReference)(using Ox): Flow[V]
  def registerResult[V](produced: ResourceReference, value: V): Unit
  def registerFlowResult[V](produced: ResourceReference, value: Flow[V]): Unit

class WsNetwork(
    private val singleTied: Map[String, (String, Int)],
    private val multiTied: Map[String, Set[(String, Int)]]
) extends Network:
  private val flowResources = mutable.Map[String, Flow[String]]()
  private val valueResources = mutable.Map[String, String]()
  private val httpEndpoint = endpoint.get
    .in("values")
    .in(query[String]("path"))
    .out(stringBody)
  private val wsEndpoint = endpoint.get
    .in("flows")
    .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](OxStreams))
  private val flowRequestPipe: Pipe[String, String] = requestedPath =>
    requestedPath.flatMap(flowResources.getOrElse(_, Flow.failed(new Exception("Flow not found"))))
  private val wsServerEndpoint = wsEndpoint.handleSuccess(_ => flowRequestPipe)
  private val httpServerEndpoint = httpEndpoint
    .handleSuccess(path => valueResources.getOrElse(path, throw new Exception("Value not found")))
  private val backend = DefaultSyncBackend()

  NettySyncServer()
    .host("0.0.0.0")
    .port(8080)
    .addEndpoints(List(wsServerEndpoint, httpServerEndpoint))
    .startAndWait()

  private def useWebSocket(ws: SyncWebSocket): Flow[String] = supervised:
    val (wsSource, _) = asSourceAndSink(ws)
    Flow.fromSource(wsSource.map {
      case WebSocketFrame.Text(text, _, _) => text
      case _                               => throw new Exception("Invalid WebSocket frame")
    })

  override def registerFlowResult[V](produced: ResourceReference, value: Flow[V]): Unit =
    val producedKey = s"${produced.peerName}:${produced.index}"
    flowResources(producedKey) = value.asInstanceOf[Flow[String]]

  private def requestPeer[V](ip: String, port: Int, request: ResourceReference)(using Ox): Option[V] =
    val content = if (request.valueType == Multitier.ValueType.Flow) "flow" else "value"
    val result = basicRequest
      .get(uri"http://$ip:$port/values?$content=${request.index}")
      .response(asString)
      .send(backend)
    result.body.toOption.map(_.asInstanceOf[V]) // Deserialize the object

  override def receiveFrom[V](from: ResourceReference)(using Ox): V =
    singleTied
      .get(from.peerName)
      .flatMap((ip, port) => requestPeer(ip, port, from))
      .getOrElse(throw new Exception(s"Possible no tie to ${from.peerName}"))

  override def receiveFromAll[V](from: ResourceReference)(using Ox): Seq[V] =
    multiTied
      .get(from.peerName)
      .map(ips => par(ips.map((ip, port) => () => requestPeer(ip, port, from)).toSeq).flatten)
      .getOrElse(throw new Exception(s"Possible no tie to ${from.peerName}"))

  override def receiveFlowFrom[V](from: ResourceReference)(using Ox): Flow[V] =
    singleTied
      .get(from.peerName)
      .map: (ip, port) =>
        val response = basicRequest
          .get(uri"http://$ip:$port/flows?path=${from.index}")
          .response(asWebSocket(useWebSocket))
          .send(backend)
          .body
        response match
          case Left(error) => Flow.failed(new Exception(s"WebSocket connection failed: $error"))
          case Right(ws)   => ws.map(_.asInstanceOf[V]) // Deserialize the object
      .getOrElse(throw new Exception(s"Possible no tie to ${from.peerName}"))

  override def registerResult[V](produced: ResourceReference, value: V): Unit =
    val producedKey = s"${produced.peerName}:${produced.index}"
    valueResources(producedKey) = value.asInstanceOf[String] // Serialize the object

object test extends App:
  WsNetwork(Map(), Map())
