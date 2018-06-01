// Field_Access.cpp: Main program for the Field_Access.dll
//

#include "traceData.h"
#include "sqlite3.h"
#include <iostream>


using namespace std;
//using namespace std;
/* Global agent data structure */

typedef struct {
	/* JVMTI Environment */
	jvmtiEnv      *jvmti;
	jboolean       vm_is_dead;
	jboolean       vm_is_started;
	/* Data access Lock */
	jrawMonitorID  lock;
	/* Options */
	char           *include;
	char           *exclude;
	int             max_count;
	/* ClassInfo Table */
	//    ClassInfo      *classes;
	jint            ccount;
} GlobalAgentData;

static GlobalAgentData *gdata;

/*sqlite3 database structure*/

sqlite3 *dbFA = NULL;
sqlite3 *dbFM = NULL;
sqlite3 *dbPP = NULL;
sqlite3 *dbCG = NULL;

//String Buffer
#define BUFFERROW 100000
#define FDBSIZE 3000
#define PDBSIZE 3000
#define CDBSIZE 3000//改大了

static char FaDBBuffer[BUFFERROW][FDBSIZE];
static int FaBufferCount = 0;

static char FmDBBuffer[BUFFERROW][FDBSIZE];
static int FmBufferCount = 0;

static char PpDBBuffer[BUFFERROW][PDBSIZE];
static int PpBufferCount = 0;

static char CgDBBuffer[BUFFERROW][CDBSIZE];
static int CgBufferCount = 0;

static void writeFAdb()
{
	sqlite3_exec(dbFA, "BEGIN TRANSACTION;", NULL, NULL, NULL);
	for (int i = 0; i < FaBufferCount; i++)
	{
		sqlite3_exec(dbFA, FaDBBuffer[i], NULL, NULL, NULL);
	}
	sqlite3_exec(dbFA, "COMMIT TRANSACTION;", NULL, NULL, NULL);
	FaBufferCount = 0;
}

static void writeFMdb()
{
	sqlite3_exec(dbFM, "BEGIN TRANSACTION;", NULL, NULL, NULL);
	for (int i = 0; i < FmBufferCount; i++)
	{
		sqlite3_exec(dbFM, FmDBBuffer[i], NULL, NULL, NULL);
	}
	sqlite3_exec(dbFM, "COMMIT TRANSACTION;", NULL, NULL, NULL);
	FmBufferCount = 0;
}

static void writePPdb()
{
	sqlite3_exec(dbPP, "BEGIN TRANSACTION;", NULL, NULL, NULL);
	for (int i = 0; i < PpBufferCount; i++)
	{
		sqlite3_exec(dbPP, PpDBBuffer[i], NULL, NULL, NULL);
	}
	sqlite3_exec(dbPP, "COMMIT TRANSACTION;", NULL, NULL, NULL);
	PpBufferCount = 0;
}

static void writeCGdb()
{
	sqlite3_exec(dbCG, "BEGIN TRANSACTION;", NULL, NULL, NULL);
	for (int i = 0; i < CgBufferCount; i++)
	{
		sqlite3_exec(dbCG, CgDBBuffer[i], NULL, NULL, NULL);
	}
	sqlite3_exec(dbCG, "COMMIT TRANSACTION;", NULL, NULL, NULL);
	CgBufferCount = 0;
}

//tag to avoid no exit loop in FA event
bool InFA;

/* Enter a critical section by doing a JVMTI Raw Monitor Enter */
static void
enter_critical_section(jvmtiEnv *jvmti)
{
	(*jvmti).RawMonitorEnter(gdata->lock);
	//check_jvmti_error(jvmti, error, "Cannot enter with raw monitor");
}

/* Exit a critical section by doing a JVMTI Raw Monitor Exit */
static void
exit_critical_section(jvmtiEnv *jvmti)
{
	(*jvmti).RawMonitorExit(gdata->lock);
	//check_jvmti_error(jvmti, error, "Cannot exit with raw monitor");
}

static void JNICALL
tdFieldAccess(jvmtiEnv *jvmti_env,
	JNIEnv* jni_env,
	jthread thread,
	jmethodID method,
	jlocation location,
	jclass field_klass,
	jobject object,
	jfieldID field) {

	if (InFA == true)return;//The JNI method getFieldObject will CAUSE ANOTHER FieldAccess EVENT, need a flag to break the dead loop
	
		jvmtiError error;
		char * method_name; //generic in fact
		char * method_signature;//name in fact
		char * method_generic;//signature in fact
		jclass method_declaring_class;
		char * method_klass_signature;
		char * field_name;
		char * field_signature;
		char * field_generic;
		char * klass_signature;
		//char  dbCommand[1000];
		char fHashcode[40];//turn jint hashcode to string, easier to manage the code
						   //char mHashcode[40];
		char oHashcode[40];
		jint oHash_code;
		jint field_modifiers;
		//jint method_modifiers;
		//jobject mObject;
		//jint mHash_code;
		jobject fObject;
		jint fHash_code;
		bool isPrivate;

		error = (*jvmti_env).GetClassSignature(
			field_klass,
			&klass_signature,
			NULL
		);
		check_jvmti_error(jvmti_env, error, "Cannot getClassSignature");

		error = (*jvmti_env).GetMethodName(
			method,
			&method_name,
			&method_signature,
			&method_generic
		);
		check_jvmti_error(jvmti_env, error, "Cannot getMethodName");

		if (object != NULL)
		{
			error = (*jvmti_env).GetObjectHashCode(object, &oHash_code);
			sprintf(oHashcode, "%d", oHash_code);
		}
		else sprintf(oHashcode, "static");

		error = (*jvmti_env).GetFieldModifiers(field_klass, field, &field_modifiers);
		check_jvmti_error(jvmti_env, error, "cannot get fieldmodifiers");

		error = (*jvmti_env).GetFieldName(
			field_klass,
			field,
			&field_name,
			&field_signature,
			&field_generic
		);
		check_jvmti_error(jvmti_env, error, "Cannot getFieldName");

		if ((field_modifiers & 0x01) != 0 || (field_modifiers & 0x04) != 0 || field_modifiers == 0)
		{
			isPrivate = false;

			error = (*jvmti_env).GetMethodDeclaringClass(method, &method_declaring_class);
			check_jvmti_error(jvmti_env, error, "Cannot getMethodDeclaringClass");

			error = (*jvmti_env).GetClassSignature(method_declaring_class, &method_klass_signature, NULL);
			check_jvmti_error(jvmti_env, error, "Cannot getMethodDeclaringClassSignature");

		}

		else
		{
			isPrivate = true;
			method_klass_signature = klass_signature;
			//sprintf(mHashcode,oHashcode);
		}

		if (field_signature[0] == 'L' || field_signature[0] == '[')
		{
			if ((field_modifiers & 0x08) == 0)
			{
				InFA = true;
				fObject = (*jni_env).GetObjectField(object, field);
				InFA = false;
				if (fObject == NULL)sprintf(fHashcode, "null");
				else
				{
					error = (*jvmti_env).GetObjectHashCode(fObject, &fHash_code);
					sprintf(fHashcode, "%d", fHash_code);
				}

				(*jni_env).DeleteLocalRef(fObject);
			}
			else sprintf(fHashcode, "static");
		}
		else sprintf(fHashcode, "primitive");
		
		enter_critical_section(jvmti_env); {
		if (FaBufferCount >= BUFFERROW) writeFAdb();
		sprintf(FaDBBuffer[FaBufferCount],
			"INSERT INTO fieldAccess(cSignature,oHashcode,fSignature,fHashcode,fName,McSignature,methodName,methodSignature)VALUES('%s','%s','%s','%s','%s','%s','%s','%s')",
			klass_signature,
			oHashcode,
			field_signature,
			fHashcode,
			field_name,
			method_klass_signature,
			method_name,
			method_signature
		);
		int size = strlen(FaDBBuffer[FaBufferCount]);
		if (size > FDBSIZE) {
			cout << FaDBBuffer[FaBufferCount] << " : " << size;
			exit(0);
		}
		FaBufferCount++;
		} exit_critical_section(jvmti_env);

		error = (*jvmti_env).Deallocate((unsigned char *)klass_signature);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

		error = (*jvmti_env).Deallocate((unsigned char *)method_name);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

		error = (*jvmti_env).Deallocate((unsigned char *)method_signature);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

		error = (*jvmti_env).Deallocate((unsigned char *)method_generic);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

		if (isPrivate == false)//之前是true
		{
			error = (*jvmti_env).Deallocate((unsigned char *)method_klass_signature);
			check_jvmti_error(jvmti_env, error, "Cannot Deallocate");
		}

		error = (*jvmti_env).Deallocate((unsigned char *)field_name);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

		error = (*jvmti_env).Deallocate((unsigned char *)field_signature);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

		error = (*jvmti_env).Deallocate((unsigned char *)field_generic);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");
}

static void JNICALL
tdFieldModification(jvmtiEnv *jvmti_env,
	JNIEnv* jni_env,
	jthread thread,
	jmethodID method,
	jlocation location,
	jclass field_klass,
	jobject object,
	jfieldID field,
	char signature_type,
	jvalue new_value) {

	//enter_critical_section(jvmti_env); {
	jvmtiError error;
	char * method_name; //generic in fact
	char * method_signature;//name in fact
	char * method_generic;//signature in fact
	jclass method_declaring_class;
	char * method_klass_signature;
	char * field_name;
	char * field_signature;
	char * field_generic;
	char * klass_signature;
	//char  dbCommand[1000];
	char fHashcode[40];//turn jint hashcode to string, easier to manage the code
					   //char mHashcode[40];
	char oHashcode[40];
	char nHashcode[40];//new Object Value
	jint oHash_code;
	jint field_modifiers;
	jobject fObject;
	jint fHash_code;
	bool isPrivate;
	//stdout_message("%c\n",signature_type);
	error = (*jvmti_env).GetClassSignature(
		field_klass,
		&klass_signature,
		NULL
	);
	check_jvmti_error(jvmti_env, error, "Cannot getClassSignature");

	error = (*jvmti_env).GetMethodName(
		method,
		&method_name,
		&method_signature,
		&method_generic
	);
	check_jvmti_error(jvmti_env, error, "Cannot getMethodName");

	if (object != NULL)
	{
		error = (*jvmti_env).GetObjectHashCode(object, &oHash_code);
		sprintf(oHashcode, "%d", oHash_code);
	}
	else
		sprintf(oHashcode, "static");

	error = (*jvmti_env).GetFieldModifiers(field_klass, field, &field_modifiers);
	check_jvmti_error(jvmti_env, error, "cannot get fieldmodifiers");

	error = (*jvmti_env).GetFieldName(
		field_klass,
		field,
		&field_name,
		&field_signature,
		&field_generic
	);
	check_jvmti_error(jvmti_env, error, "Cannot getFieldName");

	if ((field_modifiers & 0x01) != 0 || (field_modifiers & 0x04) != 0 || field_modifiers == 0)
	{
		isPrivate = false;

		error = (*jvmti_env).GetMethodDeclaringClass(method, &method_declaring_class);
		check_jvmti_error(jvmti_env, error, "Cannot getMethodDeclaringClass");

		error = (*jvmti_env).GetClassSignature(method_declaring_class, &method_klass_signature, NULL);
		
	}

	else
	{
		isPrivate = true;
		method_klass_signature = klass_signature;
		//sprintf(mHashcode,oHashcode);
	}

	if (field_signature[0] == 'L' || field_signature[0] == '[')
	{
		if ((field_modifiers & 0x08) == 0)
		{
			InFA = true;
			fObject = (*jni_env).GetObjectField(object, field);
			InFA = false;
			if (fObject == NULL)sprintf(fHashcode, "null");
			else
			{
				error = (*jvmti_env).GetObjectHashCode(fObject, &fHash_code);
				sprintf(fHashcode, "%d", fHash_code);
			}

			(*jni_env).DeleteLocalRef(fObject);

		}
		else sprintf(fHashcode, "static");
	}
	else sprintf(fHashcode, "primitive");

	if (signature_type == 'L')
	{
		jint nHash_code;
		if (new_value.l == NULL)sprintf(nHashcode, "null");
		else
		{
			error = (*jvmti_env).GetObjectHashCode(new_value.l, &nHash_code);
			sprintf(nHashcode, "%d", nHash_code);
		}
	}
	else sprintf(nHashcode, "write");

	enter_critical_section(jvmti_env); {
	if (FmBufferCount >= BUFFERROW) writeFMdb();
	sprintf(FmDBBuffer[FmBufferCount],
		"INSERT INTO fieldModification(cSignature,oHashcode,fSignature,fHashcode,fName,McSignature,methodName,methodSignature,newValue)VALUES('%s','%s','%s','%s','%s','%s','%s','%s','%s')",
		klass_signature,
		oHashcode,
		field_signature,
		fHashcode,
		field_name,
		method_klass_signature,
		method_name,
		method_signature,
		nHashcode
	);
	int size = strlen(FmDBBuffer[FmBufferCount]);
	if (size > FDBSIZE) {
		cout << FmDBBuffer[FmBufferCount] << " : " << size << endl;
		exit(0);
	}
	FmBufferCount++;
	} exit_critical_section(jvmti_env);

	//stdout_message("%s\n",dbCommand);

	error = (*jvmti_env).Deallocate((unsigned char *)klass_signature);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	error = (*jvmti_env).Deallocate((unsigned char *)method_name);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	error = (*jvmti_env).Deallocate((unsigned char *)method_signature);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	error = (*jvmti_env).Deallocate((unsigned char *)method_generic);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	if (isPrivate == false)//之前是true
	{
		error = (*jvmti_env).Deallocate((unsigned char *)method_klass_signature);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");
	}

	error = (*jvmti_env).Deallocate((unsigned char *)field_name);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	error = (*jvmti_env).Deallocate((unsigned char *)field_signature);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	error = (*jvmti_env).Deallocate((unsigned char *)field_generic);
check_jvmti_error(jvmti_env, error, "Cannot Deallocate");


	//} exit_critical_section(jvmti_env);

}

static void JNICALL
tdClassPrepare(jvmtiEnv *jvmti_env,
	JNIEnv* jni_env,
	jthread thread,
	jclass klass) {

	char * signature;
	char * generi;
	jint fieldCount;
	jfieldID * fields;
	//char * fieldName;
	jvmtiError error;
	jint modifiers;

	//enter_critical_section(jvmti_env); {
	error = (*jvmti_env).GetClassSignature(klass, &signature, &generi);
	check_jvmti_error(jvmti_env, error, "Cannot getClassSignature");

	error = (*jvmti_env).GetClassFields(klass, &fieldCount, &fields);
	check_jvmti_error(jvmti_env, error, "Cannot getClassFields");

	
	if (strstr(signature, "drools") != NULL) {//::FOR iTrust
		for (int i = 0; i < fieldCount; i++)
		{
			jfieldID field = *(fields + i);

			error = (*jvmti_env).GetFieldModifiers(klass, field, &modifiers);
			check_jvmti_error(jvmti_env, error, "Cannot get FieldModifiers");

			error = (*jvmti_env).SetFieldAccessWatch(klass, field);
			check_jvmti_error(jvmti_env, error, "Cannot setFieldAccessWatch");

			if ((modifiers & 0x10) == 0)
			{
				error = (*jvmti_env).SetFieldModificationWatch(klass, field);
				check_jvmti_error(jvmti_env, error, "Cannot setFieldModificationWatch");
			}
		}
	}

	error = (*jvmti_env).Deallocate((unsigned char *)signature);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	error = (*jvmti_env).Deallocate((unsigned char *)generi);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	error = (*jvmti_env).Deallocate((unsigned char *)fields);
	check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	//} exit_critical_section(jvmti_env);
}

void JNICALL
tdMethodEntry(jvmtiEnv *jvmti_env,
	JNIEnv* jni_env,
	jthread thread,
	jmethodID method
)
{
	    jvmtiError error;
		//method 
		char * method_name;
		char * method_signature;

		//method's declaring class
		jclass declaring_klass;
		char * klass_signature;

		//char  dbCommand[500];

		jint threadHashcode;
		char threadID[20];

		error = (*jvmti_env).GetMethodDeclaringClass(method, &declaring_klass);
		check_jvmti_error(jvmti_env, error, "Cannot get MethodDeclaringKlass");

		error = (*jvmti_env).GetClassSignature(declaring_klass, &klass_signature, NULL);
		check_jvmti_error(jvmti_env, error, "Cannot getClassSignature");

		
		if (strstr(klass_signature, "derby") != NULL) {//::FOR iTrust
																	//if(strstr(klass_signature, "Lsample") !=NULL){

			error = (*jvmti_env).GetMethodName(method, &method_name, &method_signature, NULL);
			check_jvmti_error(jvmti_env, error, "Cannot get MethodName");

			error = (*jvmti_env).GetObjectHashCode(thread, &threadHashcode);
			check_jvmti_error(jvmti_env, error, "Cannot get MethodName");

			sprintf(threadID, "%d", threadHashcode);

			enter_critical_section(jvmti_env); {
			if (CgBufferCount >= BUFFERROW) writeCGdb();
			sprintf(CgDBBuffer[CgBufferCount],
				"INSERT INTO callGraph(callFlag, classSignature, methodName, methodSignature, threadID) VALUES('%s','%s','%s','%s','%s')",
				"E",
				klass_signature,
				method_name,
				method_signature,
				threadID
			);
			int size = strlen(CgDBBuffer[CgBufferCount]);
			if (size > FDBSIZE) {
				cout << CgDBBuffer[CgBufferCount] << " : " << size << endl;
				exit(0);
			}
			CgBufferCount++;
			} exit_critical_section(jvmti_env);

			error = (*jvmti_env).Deallocate((unsigned char *)method_name);
			check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

			error = (*jvmti_env).Deallocate((unsigned char *)method_signature);
			check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

		}
		error = (*jvmti_env).Deallocate((unsigned char *)klass_signature);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

	
}

void JNICALL
tdMethodExit(jvmtiEnv *jvmti_env,
	JNIEnv* jni_env,
	jthread thread,
	jmethodID method,
	jboolean was_popped_by_exception,
	jvalue return_value)
{
		jvmtiError error;
		//method 
		char * method_name;
		char * method_signature;
		//char * method_generic ;
		//method's declaring class
		jclass declaring_klass;
		char * klass_signature;
		jint local_count;
		jvmtiLocalVariableEntry * Var_table;
		//locals
		char * local_name;
		char * local_signature;
		//char * local_generic_signature;
		jint local_slot;
		jint local_depth;
		//one particular local
		jobject  local_object;

		jint threadHashcode;
		char  threadID[20];

		error = (*jvmti_env).GetMethodDeclaringClass(method, &declaring_klass);
		check_jvmti_error(jvmti_env, error, "Cannot get MethodDeclaringKlass");

		error = (*jvmti_env).GetClassSignature(declaring_klass, &klass_signature, NULL);
		check_jvmti_error(jvmti_env, error, "Cannot getClassSignature");

		
		if (strstr(klass_signature, "derby") != NULL) {//::FOR iTrust

			error = (*jvmti_env).GetObjectHashCode(thread, &threadHashcode);
			check_jvmti_error(jvmti_env, error, "Cannot get MethodName");

			sprintf(threadID, "%d", threadHashcode);

			error = (*jvmti_env).GetMethodName(method, &method_name, &method_signature, NULL);
			//error = (*jvmti_env).GetMethodName(method, &method_name, &method_signature, &method_generic);
			check_jvmti_error(jvmti_env, error, "Cannot get MethodName");
			
			enter_critical_section(jvmti_env); {
			if (CgBufferCount >= BUFFERROW) writeCGdb();
			sprintf(CgDBBuffer[CgBufferCount],
				"INSERT INTO callGraph(callFlag, classSignature, methodName, methodSignature, threadID) VALUES('%s','%s','%s','%s','%s')",
				"X",
				klass_signature,
				method_name,
				method_signature,
				threadID
			);
			int size = strlen(CgDBBuffer[CgBufferCount]);
			if (size > FDBSIZE) {
				cout << CgDBBuffer[CgBufferCount] << " : " << size << endl;
				exit(0);
			}
			CgBufferCount++;
			} exit_critical_section(jvmti_env);
			
			error = (*jvmti_env).GetLocalVariableTable(method, &local_count, &Var_table);
			//check_jvmti_error(jvmti_env, error, "Cannot get LocalTable");
			if (error == JVMTI_ERROR_ABSENT_INFORMATION)  local_count = 0;
			

			//show locals, skip slot 0
			enter_critical_section(jvmti_env); {
			for (int i = 1; i < local_count; i++)
			{
				//if(i == 0)continue;
				local_depth = 0;
				jvmtiLocalVariableEntry * temp = Var_table + i;
				local_name = temp->name;
				local_signature = temp->signature;
				local_slot = temp->slot;

				if (local_signature[0] == 'L' || local_signature[0] == '[') {

					if (strstr(local_signature, "Exception;") != NULL)continue;

					error = (*jvmti_env).GetLocalObject(thread, local_depth, local_slot, &local_object);
					//if the local cannot be found at the current slot, try another depth
					while (error == JVMTI_ERROR_INVALID_SLOT && error != JVMTI_ERROR_NO_MORE_FRAMES)
					{
						error = (*jvmti_env).GetLocalObject(thread, ++local_depth, local_slot, &local_object);
					}
					//if the local can not be found in any frames, skip this local
					if (error != JVMTI_ERROR_NONE)continue;

					//local_hashcode = (*jni_env).CallIntMethod(local_object,jHashCode);
					jint local_hashcode;
					error = (*jvmti_env).GetObjectHashCode(local_object, &local_hashcode);
					if (error != JVMTI_ERROR_NONE)continue;
                     
					
					if (PpBufferCount >= BUFFERROW) writePPdb();
					sprintf(PpDBBuffer[PpBufferCount],
						"INSERT INTO parameterPass(fSignature,fHashcode,fName,McSignature,methodName,methodSignature)VALUES('%s','%d','%s','%s','%s','%s')",
						local_signature,
						local_hashcode,
						local_name,
						klass_signature,
						method_name,
						method_signature
					);
					int size = strlen(PpDBBuffer[PpBufferCount]);
					if (size > FDBSIZE) {
						cout << PpDBBuffer[PpBufferCount] << " : " << size << endl;
						exit(0);
					}
					PpBufferCount++;
					
				}
			
				error = (*jvmti_env).Deallocate((unsigned char *)local_name);
				check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

				error = (*jvmti_env).Deallocate((unsigned char *)local_signature);
				check_jvmti_error(jvmti_env, error, "Cannot Deallocate");
			}
		} exit_critical_section(jvmti_env);
			//record return value,ignore the non-object value and the exception situation
			jint return_hashcode;
			char  * returnType;
			char tempSignature[1000];
			
			strcpy(tempSignature, method_signature);
			int tempLen = strlen(tempSignature);
			if (tempLen > 1000) {
				cout << "tempLen"<< " : " << tempLen << endl;
				exit(0);
			}	
			
			//get return Type
			returnType = strtok(tempSignature, ")");
			returnType = strtok(NULL, ")");
			char rHashcode[40];
			//stdout_message("%s\n",returnType);
			if (was_popped_by_exception == false && (returnType[0] == 'L' || returnType[0] == '['))
			{
				if (return_value.l == NULL)sprintf(rHashcode, "null");
				else
				{
					error = (*jvmti_env).GetObjectHashCode(return_value.l, &return_hashcode);
					check_jvmti_error(jvmti_env, error, "Cannot get ReturnHashcode");
					sprintf(rHashcode, "%d", return_hashcode);
				}

				enter_critical_section(jvmti_env); {
				if (PpBufferCount >= BUFFERROW) writePPdb();
				sprintf(PpDBBuffer[PpBufferCount],
					"INSERT INTO parameterPass(fSignature,fHashcode,fName,McSignature,methodName,methodSignature)VALUES('%s','%s','%s','%s','%s','%s')",
					returnType,
					rHashcode,
					"CurMReturnValue",
					klass_signature,
					method_name,
					method_signature
				);
				int size = strlen(PpDBBuffer[PpBufferCount]);
				if (size > FDBSIZE) {
					cout << PpDBBuffer[PpBufferCount] << " : " << size << endl;
					exit(0);
				}
				PpBufferCount++;
				} exit_critical_section(jvmti_env);
			}

			error = (*jvmti_env).Deallocate((unsigned char *)method_name);
			check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

			error = (*jvmti_env).Deallocate((unsigned char *)method_signature);
			check_jvmti_error(jvmti_env, error, "Cannot Deallocate");

			if (local_count != 0)
			{
				error = (*jvmti_env).Deallocate((unsigned char *)Var_table);
				check_jvmti_error(jvmti_env, error, "Cannot Deallocate");
			}

		}
		error = (*jvmti_env).Deallocate((unsigned char *)klass_signature);
		check_jvmti_error(jvmti_env, error, "Cannot Deallocate");
}


JNIEXPORT jint JNICALL
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
	static GlobalAgentData data;
	jint                rc;
	jvmtiError          err;
	jvmtiCapabilities   capabilities;
	jvmtiEventCallbacks callbacks;
	jvmtiEnv           *jvmti;
	
	printf("ri le gou le \n");

	InFA = false;

	/* Setup initial global agent data area
	*   Use of static/extern data should be handled carefully here.
	*   We need to make sure that we are able to cleanup after ourselves
	*     so anything allocated in this library needs to be freed in
	*     the Agent_OnUnload() function.
	*/
	(void)memset((void*)&data, 0, sizeof(data));
	gdata = &data;

	/* Get JVMTI environment */
	jvmti = NULL;
	//rc = (*vm)->GetEnv(vm, (void **)&jvmti, JVMTI_VERSION);
	rc = (*vm).GetEnv((void **)&jvmti, JVMTI_VERSION);
	if (rc != JNI_OK) {
		fatal_error("ERROR: Unable to create jvmtiEnv, error=%d\n", rc);
		return -1;
	}
	if (jvmti == NULL) {
		fatal_error("ERROR: No jvmtiEnv* returned from GetEnv\n");
	}

	/* Get/Add JVMTI capabilities */
	(void)memset(&capabilities, 0, sizeof(capabilities));
	//    capabilities.can_tag_objects = 1;
	capabilities.can_generate_field_access_events = 1;
	capabilities.can_generate_field_modification_events = 1;
	capabilities.can_access_local_variables = 1;
	capabilities.can_generate_method_exit_events = 1;
	capabilities.can_generate_method_entry_events = 1;
	capabilities.can_get_source_file_name = 1;
	//err = (*jvmti)->AddCapabilities(jvmti, &capabilities);
	err = (*jvmti).AddCapabilities(&capabilities);
	//check_jvmti_error(jvmti, err, "add capabilities");

	/* Create the raw monitor */
	//err = (*jvmti)->CreateRawMonitor(jvmti, "agent lock", &(gdata->lock));
	err = (*jvmti).CreateRawMonitor("agent lock", &(gdata->lock));
	check_jvmti_error(jvmti, err, "create raw monitor");

	/* Set callbacks and enable event notifications */
	memset(&callbacks, 0, sizeof(callbacks));
	//    callbacks.DataDumpRequest         = &dataDumpRequest;
	callbacks.ClassPrepare = &tdClassPrepare;
	callbacks.FieldAccess = &tdFieldAccess;
	callbacks.FieldModification = &tdFieldModification;
	callbacks.MethodEntry = &tdMethodEntry;
	callbacks.MethodExit = &tdMethodExit;
	//callbacks.VMDeath = &tdVMDeath;
	//callbacks.DataDumpRequest = &tdDataDumpRequest;
	//err = (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof(callbacks));
	err = (*jvmti).SetEventCallbacks(&callbacks, sizeof(callbacks));
	check_jvmti_error(jvmti, err, "set event callbacks");

	err = (*jvmti).SetEventNotificationMode(JVMTI_ENABLE,
		JVMTI_EVENT_FIELD_ACCESS, NULL);
	check_jvmti_error(jvmti, err, "set event notifications FIELD ACCESS");

	err = (*jvmti).SetEventNotificationMode(JVMTI_ENABLE,
		JVMTI_EVENT_CLASS_PREPARE, NULL);
	check_jvmti_error(jvmti, err, "set event notifications CLASS_PREPARE");

	err = (*jvmti).SetEventNotificationMode(JVMTI_ENABLE,
		JVMTI_EVENT_FIELD_MODIFICATION, NULL);
	check_jvmti_error(jvmti, err, "set event notifications FIELD Modification");

	err = (*jvmti).SetEventNotificationMode(JVMTI_ENABLE,
		JVMTI_EVENT_METHOD_ENTRY, NULL);
	check_jvmti_error(jvmti, err, "set event notifications METHOD ENTRY");

	err = (*jvmti).SetEventNotificationMode(JVMTI_ENABLE,
		JVMTI_EVENT_METHOD_EXIT, NULL);
	check_jvmti_error(jvmti, err, "set event notifications METHOD EXIT");

	err = (*jvmti).SetEventNotificationMode(JVMTI_ENABLE,
		JVMTI_EVENT_VM_DEATH, NULL);
	check_jvmti_error(jvmti, err, "set event notifications VM DEATH");

	err = (*jvmti).SetEventNotificationMode(JVMTI_ENABLE,
		JVMTI_EVENT_DATA_DUMP_REQUEST, NULL);

	//DB for FA
	int dbflag = sqlite3_open("/home/zzf/sqliteOutput/test1.db", &dbFA);

	if (dbflag) {
		fprintf(stderr, "Can't open database: %s \n", sqlite3_errmsg(dbFA));
		sqlite3_close(dbFA);
		exit(1);
	}
	else printf("open test.db successfully! \n");
	char * errorDB;
	int tableflag = sqlite3_exec(dbFA, "CREATE TABLE fieldAccess(cSignature,oHashcode,fSignature,fHashcode,fName,\
                              McSignature,methodName,methodSignature,\
							  constraint cons_01 unique (cSignature,oHashcode,fSignature,fHashcode,fName,McSignature,methodName,methodSignature));"
		, NULL, NULL, &errorDB);
	if (tableflag) printf("%s", errorDB);

	//DB for FM
	dbflag = sqlite3_open("/home/zzf/sqliteOutput/test2.db", &dbFM);
	if (dbflag) {
		fprintf(stderr, "Can't open database: %s \n", sqlite3_errmsg(dbFM));
		sqlite3_close(dbFM);
		exit(1);
	}
	else printf("open test.db successfully! \n");

	tableflag = sqlite3_exec(dbFM, "CREATE TABLE fieldModification(cSignature,oHashcode,fSignature,fHashcode,fName,\
                              McSignature,methodName,methodSignature,newValue,\
							  constraint cons_01 unique (cSignature,oHashcode,fSignature,fHashcode,fName,McSignature,methodName,methodSignature,newValue));"
		, NULL, NULL, &errorDB);
	if (tableflag) printf("%s", errorDB);

	//DB for PP
	dbflag = sqlite3_open("/home/zzf/sqliteOutput/test3.db", &dbPP);
	if (dbflag) {
		fprintf(stderr, "Can't open database: %s \n", sqlite3_errmsg(dbPP));
		sqlite3_close(dbPP);
		exit(1);
	}
	else printf("open test.db successfully! \n");

	tableflag = sqlite3_exec(dbPP, "CREATE TABLE parameterPass(fSignature,fHashcode,fName,\
                              McSignature,methodName,methodSignature,\
							  constraint cons_01 unique (fSignature,fHashcode,fName,McSignature,methodName,methodSignature));"
		, NULL, NULL, &errorDB);
	if (tableflag) printf("%s", errorDB);

	//DB for CG
	dbflag = sqlite3_open("/home/zzf/sqliteOutput/CallGraph.db", &dbCG);
	if (dbflag) {
		fprintf(stderr, "Can't open database: %s \n", sqlite3_errmsg(dbCG));
		sqlite3_close(dbCG);
		exit(1);
	}
	else printf("open test.db successfully! \n");

	sqlite3_exec(dbCG, "CREATE TABLE callGraph(callFlag,classSignature, methodName, methodSignature, threadID);"
		, NULL, NULL, NULL);
	
	return JNI_OK;
}
JNIEXPORT void JNICALL
Agent_OnUnload(JavaVM *vm)
{
	//if(!isUnloaded)
	//{
	cout << "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" << endl;
	
	writeFAdb();
	sqlite3_close(dbFA);

	writeFMdb();
	sqlite3_close(dbFM);

	writePPdb();
	sqlite3_close(dbPP);

	writeCGdb();
	sqlite3_close(dbCG);
	
}


