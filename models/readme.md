models path
```shell
wget https://github.com/litongjava/tools-ocr/releases/download/model-ppocr-v4/ch_PP-OCRv4_det_infer-onnx.zip
wget https://github.com/litongjava/tools-ocr/releases/download/model-ppocr-v4/ch_PP-OCRv4_rec_infer-onnx.zip
mkdir -p models/ch_PP-OCRv4_det_infer
mkdir -p models/ch_PP-OCRv4_rec_infer
unzip ch_PP-OCRv4_det_infer-onnx.zip -d models/ch_PP-OCRv4_det_infer
unzip ch_PP-OCRv4_rec_infer-onnx.zip -d models/ch_PP-OCRv4_rec_infer
```