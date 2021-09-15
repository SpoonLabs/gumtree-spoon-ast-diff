import java.util.List;

class MultipleNesting<A, B> extends SuperClass<List<B>> {
    // 4 levels nested list
    List<List<List<List<String>>>> list;
}
