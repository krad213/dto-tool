package su.kore.dto.tool

data class Config(
        val jarLocations: List<String>,
        val classNames: List<String>,
        val targetPackage: String,
        val targetDirectory: String,
        val entityPackages: List<String>
) {
    companion object {
        private var instance: Config? = null
        fun get(): Config {
            val inst = instance;
            if (inst != null) {
                return inst
            } else {
                throw RuntimeException("not ready yet")
            }
        }

        fun set(config: Config) {
            this.instance = config
        }
    }
}