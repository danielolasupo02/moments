package com.journal.journalbackend.dto.response;

import java.util.List;

public class EntryHistoryResponse {
    private EntryResponse current;
    private List<EntryVersionResponse> versions;

    public EntryResponse getCurrent() {
        return current;
    }

    public void setCurrent(EntryResponse current) {
        this.current = current;
    }

    public List<EntryVersionResponse> getVersions() {
        return versions;
    }

    public void setVersions(List<EntryVersionResponse> versions) {
        this.versions = versions;
    }
}

