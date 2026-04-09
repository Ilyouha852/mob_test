package com.example.mobdev_lab3.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.DaoException;

import java.util.List;

import com.example.mobdev_lab3.database.dao.DaoSession;
import com.example.mobdev_lab3.database.dao.TagDao;
import com.example.mobdev_lab3.database.dao.FileMetadataDao;
import com.example.mobdev_lab3.database.dao.FileMetadataTagDao;

/**
 * Entity класс для хранения метаданных файлов
 */
@Entity(
    active = true,
    nameInDb = "FILE_METADATA"
)
public class FileMetadata {
    
    @Id(autoincrement = true)
    private Long id;
    
    @Index(unique = true)
    @NotNull
    private String filePath;
    
    @NotNull
    private String fileName;
    
    private Long lastAccessDate;
    
    private Boolean isFavorite;
    
    @ToMany(referencedJoinProperty = "fileMetadataId")
    private List<FileMetadataTag> fileMetadataTags;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1793875289)
    private transient FileMetadataDao myDao;
    


    public FileMetadata() {
    }

    @Generated(hash = 1382133607)
    public FileMetadata(Long id, @NotNull String filePath, @NotNull String fileName,
            Long lastAccessDate, Boolean isFavorite) {
        this.id = id;
        this.filePath = filePath;
        this.fileName = fileName;
        this.lastAccessDate = lastAccessDate;
        this.isFavorite = isFavorite;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getLastAccessDate() {
        return this.lastAccessDate;
    }

    public void setLastAccessDate(Long lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public Boolean getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1705674558)
    public List<FileMetadataTag> getFileMetadataTags() {
        if (fileMetadataTags == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FileMetadataTagDao targetDao = daoSession.getFileMetadataTagDao();
            List<FileMetadataTag> fileMetadataTagsNew = targetDao
                    ._queryFileMetadata_FileMetadataTags(id);
            synchronized (this) {
                if (fileMetadataTags == null) {
                    fileMetadataTags = fileMetadataTagsNew;
                }
            }
        }
        return fileMetadataTags;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1966847204)
    public synchronized void resetFileMetadataTags() {
        fileMetadataTags = null;
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
    @Generated(hash = 1305346015)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFileMetadataDao() : null;
    }


}
