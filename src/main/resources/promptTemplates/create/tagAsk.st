Given the album title {title} by artists {artists}, format is {format}, analyze and categorize it using tags.

Use the following sources IN THIS PRIORITY ORDER:
1) Available passed tags with categories (highest priority) — always try to map to these first.
2) Knowledge from RAG context, if provided by the system.
3) Your own general AI knowledge (use only if 1 and 2 do not cover the case).

Available tag categories:
{availableTagCategories}

Already existing tags to choose from for each category (priority source #1):
{availableTags}

Guidelines:
- Prefer exact or closest semantic matches from the available tags (source #1). If multiple match, choose the most specific and commonly used.
- If no suitable tag exists in source #1, consult RAG context (source #2) for domain-specific tags or conventions.
- Only if neither source #1 nor source #2 provides a suitable tag, propose a new tag using your AI knowledge (source #3).
- Ensure each proposed tag is assigned the correct category from the provided categories list.

Important Note! If your proposed tag does not exist among the available tags:
- include it in the response and mark exists boolean as false
- pick a shade color for it (use CSS Named Colors palette); if tag does exist then leave shade as null

Output: Return a structured list of tags where each item includes: category, exists, name, shade. Do not add extra commentary.