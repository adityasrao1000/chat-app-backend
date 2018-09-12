package webchat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ChatUserJDBCTemplate {
	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplateObject;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplateObject = new JdbcTemplate(dataSource);
	}

	public List<UserDetails> isAuthenticated(String username, String password) {
		String SQL = "select username from users where username = ? and password = ?";
		List<UserDetails> user = jdbcTemplateObject.query(SQL, new Object[] { username, password }, new UserMapper());
		return user;
	}

	private static class UserMapper implements RowMapper<UserDetails> {
		public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
			UserDetails user = new UserDetails();
			user.setUsername(rs.getString("username"));
			return user;
		}
	}
}
