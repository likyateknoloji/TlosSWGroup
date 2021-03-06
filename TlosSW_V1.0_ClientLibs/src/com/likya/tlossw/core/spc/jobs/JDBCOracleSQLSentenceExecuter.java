package com.likya.tlossw.core.spc.jobs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.dbconnections.DbConnectionProfileDocument.DbConnectionProfile;
import com.likya.tlos.model.xmlbeans.dbconnections.DbPropertiesDocument.DbProperties;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlos.model.xmlbeans.state.StatusNameDocument.StatusName;
import com.likya.tlos.model.xmlbeans.state.SubstateNameDocument.SubstateName;
import com.likya.tlossw.core.spc.helpers.ParamList;
import com.likya.tlossw.core.spc.model.JobRuntimeProperties;
import com.likya.tlossw.utils.GlobalRegistry;

public class JDBCOracleSQLSentenceExecuter extends JDBCSQLSentenceExecuter {

	private static final long serialVersionUID = -1947157346281291622L;

	Logger myLogger = Logger.getLogger(this.getClass());
	transient Logger globalLogger;

	private boolean retryFlag = true;

	transient protected Process process;

	public JDBCOracleSQLSentenceExecuter(GlobalRegistry globalRegistry, Logger globalLogger, JobRuntimeProperties jobRuntimeProperties) {
		super(globalRegistry, globalLogger, jobRuntimeProperties);
	}

	public void localRun() {

		initStartUp(myLogger);

		JobProperties jobProperties = getJobRuntimeProperties().getJobProperties();
		DbProperties dbProperties = getJobRuntimeProperties().getDbProperties();
		DbConnectionProfile dbConnectionProfile = getJobRuntimeProperties().getDbConnectionProfile();
		ArrayList<ParamList> myParamList = new ArrayList<ParamList>();
		
		while (true) {

			try {

				startWathcDogTimer();

				initOracleDbConnection(dbProperties, dbConnectionProfile);

				// String sqlStoredProcedureSchemaName = jobProperties.getBaseJobInfos().getJobInfos().getJobTypeDetails().getSpecialParameters().getInParam().getParameterArray(0).getValueString();
				// qString="select * from public.test_m()";
				String sqlSentence = jobProperties.getBaseJobInfos().getJobTypeDetails().getSpecialParameters().getDbJobDefinition().getFreeSQLProperties().getSqlSentence();

				Statement statement = getStatement();

				ResultSet resultSet = statement.executeQuery(sqlSentence);

				System.out.println("");
				System.out.println("*****************************************");
				System.out.println("Query'nizin sonucu ...");

				String resultData = fetchResultSet(resultSet);
				
				ParamList thisParam = new ParamList(DB_RESULT, "STRING", "VARIABLE", resultData);
				myParamList.add(thisParam);

				System.out.println("*****************************************");
				System.out.println("");
				
				statement.close();
				
				insertNewLiveStateInfo(StateName.INT_FINISHED, SubstateName.INT_COMPLETED, StatusName.INT_SUCCESS);

			} catch (Exception err) {

				try {
					if(getStatement() != null) {
						getStatement().close();
					}
					if(getStatement() != null) {
						getConnection().close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				handleException(err, myLogger);
			}

			if (processJobResult(retryFlag, myLogger)) {
				retryFlag = false;
				continue;
			}

			break;
		}

		cleanUp(process, myLogger);

	}
}
