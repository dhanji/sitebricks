package com.google.sitebricks;

import java.io.File;

public class FileTemplateSource implements TemplateSource {

  private File templateFile;
  
  public FileTemplateSource(File templateFile) {
    this.templateFile = templateFile;
  }

  @Override
  public String getLocation() {
    return templateFile.getAbsolutePath();
  }
}
