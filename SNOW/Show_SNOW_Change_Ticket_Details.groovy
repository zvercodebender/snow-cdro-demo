procedure 'Show SNOW Change Ticket Details', {
  projectName = 'ServiceNow'
  timeLimit = '0'

  formalParameter 'ChangePropertySheet', defaultValue: '', {
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  step 'Get Ticket Details', {
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
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }
}