load("@rules_jvm_external//:defs.bzl", "artifact")
load("@rules_java//java:java_binary.bzl", "java_binary")
load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_java//java:java_test.bzl", "java_test")

java_library(
    name = "c2c_gee",
    srcs = glob(
        ["java/it/unibo/c2c/*.java"],
        exclude = ["java/it/unibo/c2c/CommandLineMain.java"],
    ),
    deps = [
        ":earthengine_external",
        artifact("it.unimi.dsi:fastutil"),
        artifact("org.jspecify:jspecify"),
        artifact("com.google.guava:guava"),
    ],
)

java_binary(
    name = "c2c",
    srcs = [
        "java/com/google/earthengine/api/base/ArgsBase.java",
        "java/it/unibo/c2c/CommandLineMain.java",
    ],
    main_class = "it.unibo.c2c.CommandLineMain",
    deps = [
        ":c2c_gee",
        artifact("it.unimi.dsi:fastutil"),
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
    srcs = [
        "java/com/google/earthengine/api/base/ArgsBase.java",
        "javatests/it/unibo/c2c/BottomupTest.java",
    ],
    resources = [
        "javatests/it/unibo/c2c/testdata/input.csv",
        "javatests/it/unibo/c2c/testdata/input-nbr.csv",
        "javatests/it/unibo/c2c/testdata/output.csv",
        "javatests/it/unibo/c2c/testdata/output-filtered.csv",
        "javatests/it/unibo/c2c/testdata/output-regrowth-negonly.csv",
        "javatests/it/unibo/c2c/testdata/output-regrowth-negonly-interp.csv",
        "javatests/it/unibo/c2c/testdata/output-nbr-regrowth.csv",
        "javatests/it/unibo/c2c/testdata/output-nbr-regrowth-interp.csv",
    ],
    deps = [
        ":c2c_gee",
        artifact("junit:junit"),
        artifact("it.unimi.dsi:fastutil"),
    ],
)
