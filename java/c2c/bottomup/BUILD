load("@rules_jvm_external//:defs.bzl", "artifact")

package(default_visibility = [
    "//javatests/c2c/bottomup:__subpackages__",
])

java_library(
    name = "bottomup",
    srcs = glob(["*.java"]),
    deps = [
        "//java/com/google/earthengine/api/base",
        artifact("fastutil:fastutil"),
    ],
)
