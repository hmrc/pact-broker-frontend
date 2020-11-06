package support

import org.scalatest.{LoneElement, Matchers, WordSpecLike}
import play.api.test.WsTestClient
import play.api.{Configuration, Environment}
import uk.gov.hmrc.integration.ServiceSpec
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


trait BaseISpec extends WordSpecLike with Matchers with ServiceSpec with WsTestClient with LoneElement {

  override def externalServices: Seq[String] = Seq.empty

  override def beforeAll(): Unit = {
    super.beforeAll()
  }
  override def afterAll(): Unit = {}

  val env: Environment = Environment.simple()
  val configuration: Configuration = Configuration.load(env)
  val serviceConfig: ServicesConfig = new ServicesConfig(configuration)

  implicit val timeout: Duration = 3 minutes

  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future,timeout)

  val contentAsJson: (String,String) = ("Content-Type","application/json")
}
