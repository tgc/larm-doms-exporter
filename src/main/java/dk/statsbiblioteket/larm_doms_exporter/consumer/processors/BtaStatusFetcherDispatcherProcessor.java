package dk.statsbiblioteket.larm_doms_exporter.consumer.processors;

import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.BroadcastTranscodingRecordDAO;
import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.HibernateUtil;
import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.BroadcastTranscodingRecord;
import dk.statsbiblioteket.larm_doms_exporter.cli.ExportContext;
import dk.statsbiblioteket.larm_doms_exporter.consumer.ExportRequestState;
import dk.statsbiblioteket.larm_doms_exporter.consumer.ProcessorChainElement;
import dk.statsbiblioteket.larm_doms_exporter.consumer.ProcessorException;
import dk.statsbiblioteket.larm_doms_exporter.persistence.DomsExportRecord;
import dk.statsbiblioteket.larm_doms_exporter.persistence.ExportStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

/**
 *
 */
public class BtaStatusFetcherDispatcherProcessor extends ProcessorChainElement {

    private static Logger logger = LoggerFactory.getLogger(BtaStatusFetcherDispatcherProcessor.class);

    @Override
    protected void processThis(DomsExportRecord record, ExportContext context, ExportRequestState state) throws ProcessorException {
        HibernateUtil btaHibernateUtil = HibernateUtil.getInstance(context.getBtaHibernateConfigurationFile().getAbsolutePath());
        BroadcastTranscodingRecordDAO btaDao = new BroadcastTranscodingRecordDAO(btaHibernateUtil);
        BroadcastTranscodingRecord btaRecord = btaDao.read(record.getID());
        if (btaRecord == null || btaRecord.getTranscodingState() == null) {
            logger.info("No BTA record found for " + record.getID() + ". Not exporting now.");
            this.setChildElement(null);
            return;
        } else {
            logger.debug("Checking status for BTA record:" + btaRecord);
            switch (btaRecord.getTranscodingState()) {
                case PENDING:
                    logger.info(record.getID() + " is awaiting transcoding. " + ". Not exporting just now.");
                    this.setChildElement(null);
                    break;
                case REJECTED:
                    logger.info(record.getID() + " has been rejected for transcoding. Not exporting.");
                    this.setChildElement(null);
                    record.setState(ExportStateEnum.REJECTED);
                    context.getDomsExportRecordDAO().update(record);
                    break;
                case FAILED:
                    logger.info(record.getID() + " failed during transcoding. Not exporting.");
                    this.setChildElement(null);
                    record.setState(ExportStateEnum.REJECTED);
                    context.getDomsExportRecordDAO().update(record);
                    break;
                case COMPLETE:
                    logger.info(record.getID() + " has been successfully transcoded. Will now check if export is necessary.");
                    Date broadcastStartTime = btaRecord.getBroadcastStartTime();
                    logger.debug("Start time {} for {}", broadcastStartTime, record.getID());
                    if (broadcastStartTime == null) {
                        throw new ProcessorException("Surprised to find bta record in state COMPLETED but with null broadcastStartTime:" + record.getID());
                    }
                    Date newWalltime = new Date(broadcastStartTime.getTime() + btaRecord.getStartOffset()*1000L);
                    logger.debug("Setting walltime {} for {}.", newWalltime, record.getID() );
                    state.setWalltime(newWalltime);
                    final String transcodingCommand = btaRecord.getTranscodingCommand();
                    if (transcodingCommand != null) {
                        String[] splitCommand = transcodingCommand.split("\\s");
                        String outputFileS = splitCommand[splitCommand.length -1].replaceAll("/temp", "");
                        File outputFile = new File(outputFileS);
                        if (outputFile.exists()) {
                            Long fileTimeStamp = outputFile.lastModified();
                            state.setOutputFileTimeStamp(fileTimeStamp);
                        } else {
                            logger.warn("Could not find output file: " + outputFileS);
                            state.setOutputFileTimeStamp(btaRecord.getLastTranscodedTimestamp());
                        }
                    } else {
                        logger.warn("Could not find output file from null transcoding command for {}.", record.getID());
                        state.setOutputFileTimeStamp(btaRecord.getLastTranscodedTimestamp());
                    }
                    break;
            }
        }
    }
}
