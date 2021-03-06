
name := "sensei-mt-user"

version := "0.0.1"

scalaHome := Some(file("/opt/local/scala-2.8.1"))

scalaVersion := "2.8.1"

resolvers ++= Seq(
	"Sonatype repo" at "https://oss.sonatype.org/content/repositories/releases",
	"Typesafe repo" at "http://repo.typesafe.com/typesafe/repo",
	"Ansvia repo" at "http://scala.repo.ansvia.com/releases"
)

externalResolvers ++= Seq(
	"Local maven repo" at "file:///Users/robin/.m2/repository",
	"Sonatype repo" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
	"com.senseidb" % "sensei-core" % "1.5.1-SNAPSHOT" excludeAll(ExclusionRule(organization="javax.jms")),
	"org.mongodb" %% "casbah" % "2.4.1",
	"com.ansvia" % "ansvia-logger" % "0.0.1"
)


packageBin in Compile <<= (packageBin in Compile) map { f =>
	println("deploying...")
	println("done: " + f.getAbsolutePath)
	IO.copyFile(f, new java.io.File("conf/ext/" + f.getName))
	f
}



