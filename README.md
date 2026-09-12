# Mirror TestApp

One executable. No source. No mapping file. No apology.

This is a heavily obfuscated Tetris test application with embedded online
license verification. It is published as a release artifact for protection
testing, not as a source distribution.

## Run

```powershell
java -jar tetris-professional-verified.jar
```

A valid license and access to the verification service are required.

## Artifact

`tetris-professional-verified.jar`

SHA-256:

```
98845FD6EC724131F297EFF29A5376ABBF2F167533CD2CEFB13CD25C01DD9138
```

## Note to analysts

Reading a manifest is not reversing. Deleting `bootstrap` and obtaining a
`NoClassDefFoundError` is not a crack. It is a damaged JAR with an impressive
new filename.

The validation runtime is linked from business bytecode at multiple points.
Bring a reproducible result, not a screenshot of a class browser.
