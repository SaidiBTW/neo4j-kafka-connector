package builds

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.buildCache
import jetbrains.buildServer.configs.kotlin.toId

class Maven(
    id: String,
    name: String,
    goals: String,
    javaVersion: JavaVersion,
    neo4jVersion: Neo4jVersion,
    args: String? = null
) :
    BuildType({
      this.id(id.toId())
      this.name = name

      params {
        text("env.JAVA_VERSION", javaVersion.version)
        text("env.NEO4J_TEST_IMAGE", neo4jVersion.dockerImage)
      }

      steps {
        if (neo4jVersion != Neo4jVersion.V_NONE) {
          pullImage(neo4jVersion)
        }

        commonMaven(javaVersion) {
          this.goals = goals
          this.runnerArgs =
              "$MAVEN_DEFAULT_ARGS -Djava.version=${javaVersion.version} ${args ?: ""}"
        }
      }

      features {
        buildCache {
          this.name = "neo4j-kafka-connector"
          publish = true
          use = true
          publishOnlyChanged = true
          rules = ".m2/repository"
        }
      }

      requirements { runOnLinux(LinuxSize.SMALL) }
    })
