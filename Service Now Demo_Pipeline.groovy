pipeline 'Service Now Demo', {
  projectName = 'ServiceNow'

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }

  stage 'Create Change Request', {
    colorCode = '#289ce1'
    pipelineName = 'Service Now Demo'
    task 'Create Change Request', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
	"description": "This is change request description", 
    "comments":"This is a comment"
}''',
        'correlation_display': '',
        'correlation_id': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails',
        'short_description': 'This is a CD/RO Change',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'CreateChangeRequest'
      taskType = 'PLUGIN'
    }

    task 'Create CTask ', {
      actualParameter = [
        'change_request_id': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
	"description": "This is change task description", 
    "comments":"This is a comment",
    "assignment_group":"Software"
}''',
        'correlation_display': '',
        'correlation_id': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails/CTASK/1',
        'short_description': 'Change load balancer to 10%',
      ]
      stageSummaryParameters = '[{"name":"Change Request Number","label":"Change Request Number"},{"name":"Change Task Number","label":"Change Task Number"},{"name":"Change Task Id","label":"Change Task Id"},{"name":"Change Request Id","label":"Change Request Id"}]'
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'CreateChangeTask'
      taskType = 'PLUGIN'
    }

    task 'Show Change Object', {
      command = '''import groovy.json.JsonOutput
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

ElectricFlow ef = new ElectricFlow()

ChangeNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ChangeRequestNumber\' ).property.value
CTaskNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ChangeRequestNumber\' ).property.value
ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ResponseContent\' )

println ChangeNumber + " =============================================="
def pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="

ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ResponseContent\' )

println CTaskNumber + "=============================================="
pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="
'''
      shell = 'ec-groovy'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Show Pipeline Data', {
      actualParameter = [
        'basePath': 'myPipeline',
        'myProjectName': 'ServiceNow',
        'pipelineName': 'Service Now Demo',
      ]
      subprocedure = 'pipelineTrace'
      subproject = 'Utilities'
      taskType = 'PROCEDURE'
    }
  }

  stage 'Assess', {
    colorCode = '#ff7f0e'
    pipelineName = 'Service Now Demo'
    gate 'POST', {
      task 'SNOW Approval', {
        actualParameter = [
          'Configuration': '/projects/ServiceNow/pluginConfigurations/SNOW',
          'PollingInterval': '60',
          'RecordID': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
          'TargetState': 'approved',
        ]
        gateType = 'POST'
        subprocedure = 'Poll SNOW for target state'
        subproject = 'ServiceNow'
        taskType = 'PROCEDURE'
      }
    }

    task 'Update Change to Access', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
  "assignment_group":"Software",
  "state": -4,
  "comments": "Move to ASSESS"
}''',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails',
        'record_id': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'UpdateRecord'
      taskType = 'PLUGIN'
    }

    task 'Show Assess Ticket', {
      command = '''import groovy.json.JsonOutput
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

ElectricFlow ef = new ElectricFlow()

ChangeNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ChangeRequestNumber\' ).property.value
CTaskNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ChangeRequestNumber\' ).property.value
ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ResponseContent\' )

println ChangeNumber + " =============================================="
def pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="

ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ResponseContent\' )

println CTaskNumber + "=============================================="
pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="
'''
      shell = 'ec-groovy'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Show Assess Pipeline Data', {
      actualParameter = [
        'basePath': 'myPipeline',
        'myProjectName': 'ServiceNow',
        'pipelineName': 'Service Now Demo',
      ]
      subprocedure = 'pipelineTrace'
      subproject = 'Utilities'
      taskType = 'PROCEDURE'
    }
  }

  stage 'Schedule & Implement', {
    colorCode = '#2ca02c'
    pipelineName = 'Service Now Demo'
    gate 'POST', {
      task 'Approval', {
        gateType = 'POST'
        notificationEnabled = '1'
        notificationTemplate = 'ec_default_gate_task_notification_template'
        subproject = 'ServiceNow'
        taskType = 'APPROVAL'
        approver = [
          'rick',
        ]
      }
    }

    task 'Get Change Status', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails',
        'record_id': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'GetRecord'
      taskType = 'PLUGIN'
    }

    task 'Show Approved Ticket', {
      command = '''import groovy.json.JsonOutput
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

ElectricFlow ef = new ElectricFlow()

ChangeNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ChangeRequestNumber\' ).property.value
CTaskNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ChangeRequestNumber\' ).property.value
ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ResponseContent\' )

println ChangeNumber + " =============================================="
def pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="

ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ResponseContent\' )

println CTaskNumber + "=============================================="
pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="
'''
      shell = 'ec-groovy'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Update Change to Implement', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
	"state":-1,
    "comments": "Move to Implementation"
}''',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails',
        'record_id': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'UpdateRecord'
      taskType = 'PLUGIN'
    }

    task 'Start work on CTASK', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
    "state": 2,
    "work_notes": "Start work on CTASK"
}''',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails/CTASK/1/',
        'record_id': 'change_task.$[/myPipelineRuntime/ChangeDetails/CTASK/1/ChangeRequestSysID]',
      ]
      condition = 'False'
      errorHandling = 'continueOnError'
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'UpdateRecord'
      taskType = 'PLUGIN'
    }

    task 'Finish work on CTASK', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
	"state": 3,
    "work_notes": "Finish work on CTASK",
    "close_notes": "All DONE"
}''',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails/CTASK/1/',
        'record_id': 'change_task.$[/myPipelineRuntime/ChangeDetails/CTASK/1/ChangeRequestSysID]',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'UpdateRecord'
      taskType = 'PLUGIN'
    }
  }

  stage 'Review & Close', {
    colorCode = '#d62728'
    pipelineName = 'Service Now Demo'
    task 'Update Change to Review', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
	"state":0,
    "comments": "Move to Review"
}''',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails',
        'record_id': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'UpdateRecord'
      taskType = 'PLUGIN'
    }

    task ' Show Change Review Details', {
      command = '''import groovy.json.JsonOutput
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

ElectricFlow ef = new ElectricFlow()

ChangeNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ChangeRequestNumber\' ).property.value
CTaskNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ChangeRequestNumber\' ).property.value
ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ResponseContent\' )

println ChangeNumber + " =============================================="
def pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="

ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ResponseContent\' )

println CTaskNumber + "=============================================="
pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="
'''
      shell = 'ec-groovy'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Add Review Notes', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
    "comments": "Add Review Notes",
	"close_code": "successful",
    "close_notes": "Completed Successfully"
}''',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails',
        'record_id': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'UpdateRecord'
      taskType = 'PLUGIN'
    }

    task 'Update Change to Closed', {
      actualParameter = [
        'config_name': '/projects/ServiceNow/pluginConfigurations/SNOW',
        'content': '''{
	"state": 3,
    "comments": "Move to Closed",
}''',
        'filter': '',
        'property_sheet': '/myPipelineRuntime/ChangeDetails',
        'record_id': '$[/myPipelineRuntime/ChangeDetails/ChangeRequestNumber]',
      ]
      subpluginKey = 'EC-ServiceNow'
      subprocedure = 'UpdateRecord'
      taskType = 'PLUGIN'
    }

    task ' Show Change Closed Change Details', {
      command = '''import groovy.json.JsonOutput
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

ElectricFlow ef = new ElectricFlow()

ChangeNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ChangeRequestNumber\' ).property.value
CTaskNumber = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ChangeRequestNumber\' ).property.value
ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/ResponseContent\' )

println ChangeNumber + " =============================================="
def pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="

ResponseContent = ef.getProperty( propertyName: \'/myPipelineRuntime/ChangeDetails/CTASK/1/ResponseContent\' )

println CTaskNumber + "=============================================="
pretty = JsonOutput.prettyPrint( ResponseContent.property.value )
println pretty
println "=============================================="
'''
      shell = 'ec-groovy'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }
  }

  // Custom properties

  property 'ec_counters', {

    // Custom properties
    pipelineCounter = '11'
  }
}