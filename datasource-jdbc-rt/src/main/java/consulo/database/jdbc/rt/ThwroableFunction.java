package consulo.database.jdbc.rt;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public interface ThwroableFunction<R, P>
{
	R call(P param) throws Throwable;
}
