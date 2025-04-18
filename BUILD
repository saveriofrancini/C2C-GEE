load("@rules_jvm_external//:defs.bzl", "artifact")

java_binary(
    name = "c2c_gee_cli",
    srcs = glob(["java/**/*.java"]),
    main_class = "it.unibo.c2c.CommandLineMain",
    deps = [
        artifact("it.unimi.dsi:fastutil"),
        artifact("org.jspecify:jspecify"),
        artifact("com.google.guava:guava"),
    ],
)

java_library(
    name = "c2c_gee",
    srcs = glob(["java/it/**/*.java"]),
    deps = [
        ":earthengine_external",
        artifact("it.unimi.dsi:fastutil"),
        artifact("org.jspecify:jspecify"),
        artifact("com.google.guava:guava"),
    ],
)

java_library(
    name = "earthengine_external",
    srcs = [
        "java/com/google/earthengine/api/base/ArgsBase.java",
    ],
    neverlink = 1,
)

java_test(
    name = "BottomupTest",
    size = "medium",
    srcs = glob([
        "java/com/google/earthengine/api/base/*.java",
        "javatests/it/unibo/c2c/*.java"
    ]),
    resources = glob(["javatests/it/unibo/c2c/*.csv"]),
    deps = [
        ":c2c_gee",
        artifact("junit:junit"),
        artifact("it.unimi.dsi:fastutil"),
    ],
)
