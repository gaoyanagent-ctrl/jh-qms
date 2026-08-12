# AI Tool And Evaluation Contract

## Tool Registry

| Tool | Version | Input | Output | External model |
|---|---|---|---|---|
| `pdf-vector` | `0.1.0` | PDF bytes, document id, revision | DIM 1.0 + entities + evidence | None |

Tool and prompt versions must be recorded on every evidence row. Model-assisted tools must
also record model name/version and keep prompts in a versioned registry before use.

Golden samples must be synthetic or redistributable and assert page geometry, normalized
text, bounded BBoxes, stable entity/evidence linkage, and invalid input behavior. Future
dimension/OCR evaluation must separately measure recall, field accuracy, localization, and
false-positive rate; a single aggregate score is not accepted as a release gate.
