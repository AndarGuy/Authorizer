package me.andarguy.authorizer.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@DatabaseTable(tableName = "authorization_requests")
public class AuthorizationRequest {
    @DatabaseField(id = true, canBeNull = false, columnName = "id")
    private String id;

    @DatabaseField(columnName = "name", canBeNull = false)
    private String name;

    @DatabaseField(columnName = "timestamp", canBeNull = false)
    private Long timestamp;

    @DatabaseField(columnName = "server", canBeNull = false)
    private String server;

    @DatabaseField(columnName = "ip", canBeNull = false)
    private String ip;

    @DatabaseField(columnName = "status", canBeNull = false, dataType = DataType.ENUM_INTEGER)
    private AuthorizationRequest.Status status;

    public AuthorizationRequest() {

    }

    public enum Status {
        CREATED,
        AUTHORIZED,
        BLOCKED;
    }
}
