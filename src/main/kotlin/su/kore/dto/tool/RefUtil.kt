package su.kore.dto.tool

import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.FilterBuilder


class RefUtil {
    companion object {
        private lateinit var refl: Reflections;

        fun init(cl: ClassLoader, packages: List<String>) {
            refl = Reflections(ConfigurationBuilder()
                    .addClassLoader(cl)
                    .addScanners(SubTypesScanner())
                    .addUrls(ClasspathHelper.forClassLoader(cl))
                    .filterInputsBy(FilterBuilder().includePackage(*packages.toTypedArray()))
                    .forPackages(*packages.toTypedArray()))
        }

        fun get():Reflections {
            return refl
        }
    }
}