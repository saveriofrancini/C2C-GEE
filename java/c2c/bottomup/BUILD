load("@rules_jvm_external//:defs.bzl", "artifact")

package(default_visibility = [
    "//javatests/c2c/bottomup:__subpackages__",
])

java_library(
    name = "bottomup",
    srcs = glob(["*.java"]),
    deps = [
        "//java/com/google/earthengine/api/base",
        artifact("it.unimi.dsi:fastutil"),
        artifact("org.jspecify:jspecify"),
        artifact("com.google.guava:guava"),
    ],
)
