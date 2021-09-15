import java.util.List;

interface NestedTypes<T> extends A, B<T>, C<List<List<T>>>, D { }
