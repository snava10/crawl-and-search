package com.snava.cubanews;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LuceneIndexerTest {

  Directory memoryIndex;
  Analyzer analyzer;
  IndexWriterConfig indexWriterConfig;
  Indexer indexer;
  IndexReader indexReader;
  IndexSearcher searcher;

  @BeforeEach
  void setUp() throws IOException {
    memoryIndex = new ByteBuffersDirectory();
    analyzer = new StandardAnalyzer();
    indexWriterConfig = new IndexWriterConfig(analyzer);
    indexer = new LuceneIndexer(memoryIndex, analyzer);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void close() {
  }

  @Test
  void index() throws Exception {
    IndexDocument iDocument1 = ImmutableIndexDocument.builder()
        .url("http://url1.com")
        .title("doc1")
        .text("doc1 content")
        .build();
    indexer.index(iDocument1);
    indexReader = DirectoryReader.open(memoryIndex);
    searcher = new IndexSearcher(indexReader);
    int count = searcher.count(new TermQuery(new Term("_id", "http://url1.com")));
    assertEquals(1, count);
  }

  @Test
  void index_shouldNotDuplicateDocuments() throws IOException {
    IndexDocument iDocument1 = ImmutableIndexDocument.builder()
        .url("http://url1.com")
        .title("doc1")
        .text("doc1 content")
        .build();
    indexer.index(iDocument1);
    indexer.index(iDocument1);
    indexReader = DirectoryReader.open(memoryIndex);
    searcher = new IndexSearcher(indexReader);
    int count = searcher.count(new TermQuery(new Term("_id", "http://url1.com")));
    assertEquals(1, count);
  }

  @Test
  void index_ShouldStoreLastUpdated() throws Exception {
    IndexDocument iDocument1 = ImmutableIndexDocument.builder()
        .url("http://url1.com")
        .title("doc1")
        .text("doc1 content")
        .build();
    indexer.index(iDocument1);
    indexReader = DirectoryReader.open(memoryIndex);
    searcher = new IndexSearcher(indexReader);
    TopDocs topDocs = searcher.search(new TermQuery(new Term("_id", "http://url1.com")), 1);
    String lastUpdated = searcher.doc(topDocs.scoreDocs[0].doc).get("lastUpdated");
    assertNotNull(lastUpdated);
    assertTrue(Long.parseLong(lastUpdated) > 0);
  }

  @Test
  void test() throws Exception {
    String projectId = "crawl-and-search";
    GoogleCredentials credentials = GoogleCredentials.fromStream(
            new FileInputStream("/home/sergio/datastore-key.json"))
        .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
    FirestoreOptions firestoreOptions =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId(projectId)
            .setCredentials(credentials)
            .build();
    Firestore db = firestoreOptions.getService();
    DocumentReference docRef = db.collection("pages").document("testPage");

    Map<String, Object> data = new HashMap<>();
    data.put("url", "http://url1.com");
    data.put("title", "Url 1");
    data.put("text", "Url text");

    ApiFuture<WriteResult> result = docRef.set(data);
// ...
// result.get() blocks on response
    System.out.println("Update time : " + result.get().getUpdateTime());
  }

}