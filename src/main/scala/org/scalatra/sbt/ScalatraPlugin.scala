package org.scalatra.sbt

import sbt._
import Keys._
import java.net.URI
import com.earldouglas.xwp.ContainerPlugin.start
import com.earldouglas.xwp.ContainerPlugin.autoImport.{ Container, containerPort }
import com.earldouglas.xwp.JettyPlugin
import com.earldouglas.xwp.JettyPlugin.{ projectSettings => jettySettings }
import com.earldouglas.xwp.JettyPlugin.autoImport.Jetty

object ScalatraPlugin extends AutoPlugin {
  override def requires = JettyPlugin

  val autoImport = PluginKeys

  import autoImport._

  val browseTask = browse := {
    val log = streams.value.log

    // read port for jetty, default to 8080
    val port = {
      val p = (containerPort in Jetty).value
      if (p == -1) 8080 else p
    }

    val url = URI.create("http://localhost:%s" format port)
    try {
      log.info("Launching browser.")
      java.awt.Desktop.getDesktop.browse(url)
    } catch {
      case _: Throwable => {
        log.info(f"Could not open browser, sorry. Open manually to ${url.toASCIIString}")
      }
    }
  }

  val xwpNotification = inConfig(Container)(Seq(
    start := {
      streams.value.log.error("since xsbt-web-plugin 2.x please use jetty:start instead of container:start, for more information check the docs at https://github.com/earldouglas/xsbt-web-plugin")
      Seq(Compat.createProcess)
    }
  ))

  val scalatraSettings: Seq[Def.Setting[_]] = jettySettings ++ Seq(browseTask) ++ xwpNotification

  val scalatraWithDist: Seq[Def.Setting[_]] = scalatraSettings ++ DistPlugin.distSettings

  override lazy val projectSettings = scalatraSettings
}
