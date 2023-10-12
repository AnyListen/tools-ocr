package com.litongjava.project.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by litonglinux@qq.com on 10/11/2023_3:24 PM
 */
public class ProjectConfigTest {

  @Test
  public void getStr() {
    ProjectConfig projectConfig = new ProjectConfig();
    projectConfig.put("model", "model");
  }

  @Test
  public void getStr2() {
    ProjectConfig projectConfig = new ProjectConfig();
    String model = projectConfig.getStr("model");
    System.out.println(model);
  }
}