package org.uniqa.event;

import org.springframework.context.ApplicationEvent;
import org.uniqa.entity.TableRow;

public class RowCreatedEvent extends ApplicationEvent {
    private final TableRow row;
    public RowCreatedEvent(Object source, TableRow row) {
        super(source);
        this.row = row;
    }
    public TableRow getRow() { return row; }
}
