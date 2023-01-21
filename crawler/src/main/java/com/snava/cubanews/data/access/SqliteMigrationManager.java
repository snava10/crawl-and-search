package com.snava.cubanews.data.access;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class SqliteMigrationManager {

  private final SqliteMetadataDatabase db;

  public SqliteMigrationManager(SqliteMetadataDatabase db) {
    this.db = db;
  }

  public void runMigrations() throws Exception {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL url = classLoader.getResource("db_migrations");

    if (url == null) {
      throw new RuntimeException("The url for migrations cannot be null");
    }
    int dbVersion = db.getDatabaseVersion();
    List<Path> migrations = getOutstandingMigrations();
    if (!migrations.isEmpty()) {
      db.backupDatabase();
    }
    migrations.forEach(this::runMigration);
  }

  public List<Path> getOutstandingMigrations() throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL url = classLoader.getResource("db_migrations");

    if (url == null) {
      throw new RuntimeException("The url for migrations cannot be null");
    }
    int dbVersion = db.getDatabaseVersion();

    return Files.list(Path.of(url.getPath()))
        .sorted(Comparator.comparing(Path::getFileName))
        .filter(path -> path.getFileName().toString().startsWith("migration"))
        .filter(path -> {
          String filename = path.getFileName().toString();
          int version = Integer.parseInt(filename.split("migration")[1].split("\\.")[0]);
          return version > dbVersion;
        }).toList();

  }

  private void runMigration(Path p) {
    try {
      System.out.printf("Running migration %s\n", p);
      String sql = Files.readString(p);
      db.updateStatement(sql);
      int version = Integer.parseInt(
          p.getFileName().toString().split("migration")[1].split("\\.")[0]);
      db.setDatabaseVersion(version);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }


  public static void main(String[] args) throws Exception {
    SqliteMetadataDatabase db = new SqliteMetadataDatabase("/tmp/cubanews-test/cubanews-test.db",
        "pages");
    db.initialise();
    SqliteMigrationManager sqliteMigrationManager = new SqliteMigrationManager(db);
    sqliteMigrationManager.runMigrations();
  }

}