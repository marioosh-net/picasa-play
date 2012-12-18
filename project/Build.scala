import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-starter"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.google.gdata" % "gdata-core" % "1.0",
      "com.google.gdata" % "gdata-photos" % "2.0",
      "com.google.gdata" % "gdata-photos-meta" % "2.0",
      "com.google.gdata" % "gdata-media" % "1.0",
      "javax.mail" % "mail" % "1.4"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      resolvers <+= baseDirectory { base => 
        "my" at "file:///"+base.getAbsolutePath+"/repo"
      }
    )

}
