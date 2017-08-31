resolvers += "Sonatype OSS Releases" at
  "https://oss.sonatype.org/content/repositories/releases"

addSbtPlugin("com.github.hochgi" % "sbt-cassandra-plugin" % "0.6.2")

//resolvers += Resolver.bintrayIvyRepo("cb372", "sbt-plugins")
//addSbtPlugin("com.github.cb372" % "sbt-write-output-to-file" % "0.1")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.2.24")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.0")