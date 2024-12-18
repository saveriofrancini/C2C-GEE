java_library(
    name = "bottomup",
    srcs = glob(
        ["*.java"],
        exclude = ["Run.java"],
    ),
    deps = ["//java/com/google/earthengine/api/base"],
)

java_binary(
    name = "Run",
    srcs = ["Run.java"],
    data = glob([
        "testdata/input.csv",
        "testdata/output/*.csv",
    ]),
    deps = [
        ":bottomup",
    ],
)
