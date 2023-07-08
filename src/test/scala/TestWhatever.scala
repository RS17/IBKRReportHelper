import realized.MatchersRealized.OpenStockRowMatcher
import org.scalatest.flatspec.AnyFlatSpec

class TestWhatever extends AnyFlatSpec{
    "this test" should "return true" in {
      assert(OpenStockRowMatcher.testMe)
    }
}
