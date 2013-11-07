package dk.statsbiblioteket.larm_doms_exporter.consumer;

import org.w3c.dom.Document;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 17/04/13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class ExportRequestState {

    private String pbcoreString;
    private Document pbcoreDocument;
    private Date walltime;
    private Long changeInFileStartWalltime;

    /* ---------------------------- */

    public Document getPbcoreDocument() {
        return pbcoreDocument;
    }

    public void setPbcoreDocument(Document pbcoreDocument) {
        this.pbcoreDocument = pbcoreDocument;
    }

    public String getPbcoreString() {
        return pbcoreString;
    }

    public void setPbcoreString(String pbcoreString) {
        this.pbcoreString = pbcoreString;
    }

    public Date getWalltime() {
        return walltime;
    }

    public void setWalltime(Date walltime) {
        this.walltime = walltime;
    }

    public Long getChangeInFileStartWalltime() {
        return changeInFileStartWalltime;
    }

    public void setChangeInFileStartWalltime(Long changeInFileStartWalltime) {
        this.changeInFileStartWalltime = changeInFileStartWalltime;
    }
}
