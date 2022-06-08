package me.andarguy.authorizer.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.andarguy.authorizer.utils.CryptoUtils;

import java.util.Locale;

@AllArgsConstructor
@Getter
@Setter
@DatabaseTable(tableName = "accounts")
public class Account {
  @DatabaseField(id = true, columnName = "id")
  private String id;

  @DatabaseField(canBeNull = false, columnName = "name")
  private String name;

  @DatabaseField(canBeNull = false, columnName = "password")
  private String hashedPassword;

  @DatabaseField(canBeNull = false, columnName = "uuid")
  private String uuid;

  @DatabaseField(canBeNull = false, columnName = "authorization_type", defaultValue = "standard", dataType = DataType.ENUM_TO_STRING)
  private AuthorizationType authorizationType;

  @DatabaseField(canBeNull = false, columnName = "authorization_id")
  private Integer authorizationId;

  public enum AuthorizationType {
    STANDARD,
    INTERACTIVE;

    @Override
    public String toString() {
      return this.name().toLowerCase(Locale.ROOT);
    }
  }

  public Account() {

  }

  public boolean checkPassword(String password) {
    return CryptoUtils.checkPassword(password, this.hashedPassword);
  }
}