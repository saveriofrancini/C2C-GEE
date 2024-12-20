load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "tests",
    srcs = glob(["*.java"]),
    data = [
        "testdata/input.csv",
        "testdata/output.csv",
    ],
    deps = [
        "//java/c2c/bottomup",
        artifact("junit:junit"),
        artifact("it.unimi.dsi:fastutil"),
    ],
)

java_test(
    name = "BottomupTest",
    size = "small",
    resources = [
        "testdata/input.csv",
        "testdata/output.csv",
    ],
    runtime_deps = [
        ":tests",
    ],
)
