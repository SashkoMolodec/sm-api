Given the album title {title} by artists {artists}, and format {format}, produce tags.

Sources IN THIS PRIORITY ORDER:
1) RAG context (highest priority): extract tags explicitly present in the retrieved context that clearly relate to the provided artist(s) and/or the album title. Ignore unrelated snippets. Treat these as existing tags.
2) General AI knowledge: if there are important tags not found in RAG but commonly associated with this artist/album, append them after RAG-based tags.

Allowed tag categories (format: name: description):
{availableTagCategories}

Rules:
- Category must be one of the allowed category names (left side before the colon), in lowercase.
- Deduplicate tags and avoid near-duplicates.
- shade field: choose exactly one CSS named color, lowercase, single word; no punctuation or extra text; if unsure, use gray. Follow the same standard as the color chooser prompt.

Output format (strict):
- Return ONLY a JSON array of objects, no prose.
- Each object must include fields: category (lowercase category name), name (string), shade (null or lowercase CSS color string).
- Do not include any other fields, text, or comments.