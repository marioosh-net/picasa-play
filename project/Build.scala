import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "picasa-play"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.google.gdata" % "gdata-core" % "1.0",
      "com.google.gdata" % "gdata-photos" % "2.0",
      "com.google.gdata" % "gdata-photos-meta" % "2.0",
      "com.google.gdata" % "gdata-media" % "1.0",
      "javax.mail" % "mail" % "1.4",
      "commons-beanutils" % "commons-beanutils" % "1.8.3",
      "commons-collections" % "commons-collections" % "3.2.1",
      "net.htmlparser.jericho" % "jericho-html" % "3.1",
      "org.apache.httpcomponents" % "httpclient" % "4.2.3"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      resolvers <+= baseDirectory { base => 
        "my" at "file:///"+base.getAbsolutePath+"/repo"
      }
    )

}
