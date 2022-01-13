
rootProject.name = "simbot-component-mirai"

include(":api")
project(":api").name = "simbot-component-mirai-api"

include(":core")
project(":core").name = "simbot-component-mirai-core"

include(":boot")
project(":boot").name = "simbot-component-mirai-boot"
