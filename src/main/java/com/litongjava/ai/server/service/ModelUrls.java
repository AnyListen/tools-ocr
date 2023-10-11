package com.litongjava.ai.server.service;


/**
 * 模型下载地址https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.7/doc/doc_ch/models_list.md#1.3
 * 检测模型
 * https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_det_infer.tar
 * https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_det_server_infer.tar
 * 方向分类器
 * https://paddleocr.bj.bcebos.com/dygraph_v2.0/ch/ch_ppocr_mobile_v2.0_cls_infer.tar
 * 识别模型
 * https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_rec_infer.tar
 * https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_rec_server_infer.tar
 */
public class ModelUrls {

//  private static String detModelUrls = "https://resources.djl.ai/test-models/paddleOCR/mobile/det_db.zip";
//  private static String clsModelUrls = "https://resources.djl.ai/test-models/paddleOCR/mobile/cls.zip";
//  private static String recModelUrls = "https://resources.djl.ai/test-models/paddleOCR/mobile/rec_crnn.zip";

//  private String detModelUrls = "https://paddleocr.bj.bcebos.com/PP-OCRv3/chinese/ch_PP-OCRv3_det_infer.tar";
//  private String clsModelUrls = "https://paddleocr.bj.bcebos.com/dygraph_v2.0/ch/ch_ppocr_mobile_v2.0_cls_infer.tar";
//  private String recModelUrls = "https://paddleocr.bj.bcebos.com/PP-OCRv3/chinese/ch_PP-OCRv3_rec_infer.tar";

  public static final String clsV2 = "https://paddleocr.bj.bcebos.com/dygraph_v2.0/ch/ch_ppocr_mobile_v2.0_cls_infer.tar";

  public static final String detV4Infer = "https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_det_infer.tar";

  public static final String detV4Server = "https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_det_server_infer.tar";
  public static final String recV4Server = "https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_rec_server_infer.tar";
}
