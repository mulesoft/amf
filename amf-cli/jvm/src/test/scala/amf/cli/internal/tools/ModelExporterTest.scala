package amf.cli.internal.tools

import amf.core.io.FileAssertionTest
import org.scalatest.funsuite.AsyncFunSuite

class ModelExporterTest extends AsyncFunSuite with FileAssertionTest {

  val golden = "file://documentation/model.md"

  test("Model export hasn't been modified") {
    val modelExportText = ModelExporter.exportText()
    for {
      tmpFile   <- writeTemporaryFile(golden)(modelExportText)
      assertion <- assertDifferences(tmpFile, golden)
    } yield {
      assertion
    }
  }
}
