import sbt._
import Keys._
import play.Project._
import com.github.play2war.plugin._

object ApplicationBuild extends Build {

    val appName         = "picasa-play"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
    	javaCore,
      /*
      "com.google.gdata" % "gdata-core" % "1.0",
      "com.google.gdata" % "gdata-photos" % "2.0",
      "com.google.gdata" % "gdata-photos-meta" % "2.0",
      "com.google.gdata" % "gdata-media" % "1.0",
      */
      "javax.mail" % "mail" % "1.4",
      "commons-beanutils" % "commons-beanutils" % "1.8.3",
      "commons-collections" % "commons-collections" % "3.2.1",
      "net.htmlparser.jericho" % "jericho-html" % "3.1",
      "org.apache.httpcomponents" % "httpclient" % "4.2.3",
      // "com.aetrion.flickr" % "flickrapi" % "1.1",
      "com.flickr4java" % "flickr" % "2.5",
      "org.scribe" % "scribe" % "1.3.2",
      "log4j" % "log4j" % "1.2.17"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers <+= baseDirectory { base => 
        "my" at "file:///"+base.getAbsolutePath+"/repo"
      },
      Play2WarKeys.servletVersion := "3.0"
    ).settings(Play2WarPlugin.play2WarSettings: _*)

}
