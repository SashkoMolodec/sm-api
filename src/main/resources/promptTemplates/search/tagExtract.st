Given the user query: {userQuery}
and available tags: {tagsList}

Extract relevant tags from the query. Match exact tag names when possible. Return only matching tags as a list.
Rules:
- Match partial words if they clearly refer to tag
- Ignore common words and articles
- Consider synonyms and related terms
- Return empty list if no matches found
- Always return meaningful words/concepts as tags, even if they don't match available tags
- Keep terms in their common English form