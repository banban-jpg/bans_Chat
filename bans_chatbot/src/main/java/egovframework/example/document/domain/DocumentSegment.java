package egovframework.example.document.domain;

/** Trusted per-segment classification; a document-wide flag cannot authorize all text. */
public record DocumentSegment(String segmentId, String documentId, boolean externalAiAllowed,
                              boolean synthetic, boolean nonSensitive) { }
