-- STEP 11: document tags and bounded keyword-search access paths, Oracle 26ai only.
-- No Oracle Text index: analyzer support and plans require a real 26ai experiment.
CREATE TABLE document_tag (
    document_id NUMBER(19) NOT NULL,
    tag_value VARCHAR2(32 CHAR) NOT NULL,
    CONSTRAINT pk_document_tag PRIMARY KEY (document_id, tag_value),
    CONSTRAINT fk_document_tag_document FOREIGN KEY (document_id) REFERENCES document (document_id)
);

CREATE INDEX ix_document_tag_value ON document_tag (tag_value, document_id);
CREATE INDEX ix_document_search_current ON document (document_status, updated_at DESC, document_id DESC, current_version_id);
-- Existing ix_fragment_search supports the ai_searchable/version join. INSTR over
-- CLOB is not B-tree accelerated; measure the actual execution plan before tuning.
