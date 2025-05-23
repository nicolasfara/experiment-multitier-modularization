package it.unibo.capabilities

import io.circe.{Decoder, Encoder}
import it.unibo.capabilities.Multitier.{ResourceReference, ValueType}
import ox.*
import ox.flow.Flow
import sttp.client4.impl.ox.ws.asSourceAndSink
import sttp.client4.ws.SyncWebSocket
import sttp.client4.ws.sync.asWebSocket
import sttp.client4.{DefaultSyncBackend, UriContext, basicRequest}
import sttp.tapir.*
import sttp.tapir.server.netty.sync.OxStreams
import sttp.tapir.server.netty.sync.OxStreams.Pipe
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.ws.WebSocketFrame
import sttp.client4.circe.*
import sttp.tapir.json.circe.*
import io.circe.syntax.*
import io.circe.parser.decode
import ox.resilience.{RetryConfig, retry}

import scala.concurrent.duration.DurationInt

trait Network:
  def receiveFrom[V: Decoder](from: ResourceReference)(using Ox): V
  def receiveFromAll[V: Decoder](from: ResourceReference)(using Ox): Seq[V]
  def receiveFlowFrom[V: Decoder](from: ResourceReference)(using Ox): Flow[V]
  def registerResult[V: Encoder](produced: ResourceReference, value: V): Unit
  def registerFlowResult[V](produced: ResourceReference, value: Flow[V]): Unit
  def startNetwork(using Ox): Unit = ()

class WsNetwork(
    private val singleTied: Map[String, (String, Int)],
    private val multiTied: Map[String, Set[(String, Int)]],
    private val port: Int = 8080
) extends Network:
  private val flowResources = collection.concurrent.TrieMap[Int, Flow[Any]]()
  private val valueResources = collection.concurrent.TrieMap[Int, String]() // Already encoded
  private val httpEndpoint = endpoint.get
    .in("values")
    .in(query[Int]("path"))
    .out(stringBody)
  private val wsEndpoint = endpoint.get
    .in("flows")
    .out(webSocketBody[Int, CodecFormat.TextPlain, String, CodecFormat.Json](OxStreams))
  private def flowRequestPipe[V]: Pipe[Int, V] = requestedPath =>
    requestedPath.flatMap(
      flowResources.getOrElse(_, Flow.failed(new Exception("Flow not found"))).asInstanceOf[Flow[V]]
    )
  private val wsServerEndpoint = wsEndpoint.handleSuccess(_ => flowRequestPipe)
  private val httpServerEndpoint = httpEndpoint
    .handleSuccess(path => valueResources.getOrElse(path, throw new Exception("Value not found")))
  private val backend = DefaultSyncBackend()

  override def startNetwork(using Ox): Unit =
    NettySyncServer()
      .host("localhost")
      .port(port)
      .addEndpoints(List(wsServerEndpoint, httpServerEndpoint))
      .start()

  private def useWebSocket[V: Decoder](ws: SyncWebSocket): Flow[V] = supervised:
    val (wsSource, _) = asSourceAndSink(ws)
    Flow.fromSource(wsSource.map {
      case WebSocketFrame.Text(text, _, _) => decode[V](text).getOrElse(throw Exception("Invalid JSON"))
      case _                               => throw new Exception("Invalid WebSocket frame")
    })

  override def registerFlowResult[V](produced: ResourceReference, value: Flow[V]): Unit =
    flowResources(produced.index) = value

  private def requestPeer[V: Decoder](ip: String, port: Int, request: ResourceReference)(using Ox): Option[V] = fork:
    retry(RetryConfig.backoff(10, 500.milliseconds)):
      val result = basicRequest
        .get(uri"http://$ip:$port/values?path=${request.index}")
        .response(asJson[V])
        .send(backend)
      result.body.fold(
        error => throw Exception("Error in request: " + error),
        value => Some(value)
      )
  .join()

  override def receiveFrom[V: Decoder](from: ResourceReference)(using Ox): V =
    singleTied
      .get(from.peerName)
      .flatMap((ip, port) => requestPeer(ip, port, from))
      .getOrElse(throw new Exception(s"Possible no tie to ${from.peerName}"))

  override def receiveFromAll[V: Decoder](from: ResourceReference)(using Ox): Seq[V] =
    multiTied
      .get(from.peerName)
      .map(ips => par(ips.map((ip, port) => () => requestPeer(ip, port, from)).toSeq).flatten)
      .getOrElse(throw new Exception(s"Possible no tie to ${from.peerName}"))

  override def receiveFlowFrom[V: Decoder](from: ResourceReference)(using Ox): Flow[V] =
    singleTied
      .get(from.peerName)
      .map: (ip, port) =>
        val response = basicRequest
          .body(from.index.toString)
          .get(uri"ws://$ip:$port/flows?path")
          .response(asWebSocket(useWebSocket))
          .send(backend)
          .body
        response match
          case Left(error) => Flow.failed(new Exception(s"WebSocket connection failed: $error"))
          case Right(ws)   => ws
      .getOrElse(throw new Exception(s"Possible no tie to ${from.peerName}"))

  override def registerResult[V: Encoder](produced: ResourceReference, value: V): Unit =
    valueResources(produced.index) = value.asJson.toString
