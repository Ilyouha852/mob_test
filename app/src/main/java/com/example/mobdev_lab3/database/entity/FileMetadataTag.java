package com.example.mobdev_lab3.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import com.example.mobdev_lab3.database.dao.DaoSession;
import com.example.mobdev_lab3.database.dao.FileMetadataTagDao;

/**
 * Join entity для связи многие-ко-многим между FileMetadata и Tag
 */
@Entity(
    active = true,
    nameInDb = "FILE_METADATA_TAG"
)
public class FileMetadataTag {
    
    @Id(autoincrement = true)
    private Long id;
    
    @NotNull
    private Long fileMetadataId;
    
    @NotNull
    private Long tagId;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 2003068929)
    private transient FileMetadataTagDao myDao;

    public FileMetadataTag() {
    }

    @Generated(hash = 1611079242)
    public FileMetadataTag(Long id, @NotNull Long fileMetadataId,
            @NotNull Long tagId) {
        this.id = id;
        this.fileMetadataId = fileMetadataId;
        this.tagId = tagId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileMetadataId() {
        return this.fileMetadataId;
    }

    public void setFileMetadataId(Long fileMetadataId) {
        this.fileMetadataId = fileMetadataId;
    }

    public Long getTagId() {
        return this.tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 733413946)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFileMetadataTagDao() : null;
    }
}
