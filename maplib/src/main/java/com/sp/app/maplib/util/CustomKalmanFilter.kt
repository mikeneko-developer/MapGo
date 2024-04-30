package com.sp.app.maplib.util
import org.apache.commons.math3.filter.DefaultProcessModel
import org.apache.commons.math3.filter.DefaultMeasurementModel
import org.apache.commons.math3.filter.KalmanFilter
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.filter.MeasurementModel
import org.apache.commons.math3.filter.ProcessModel
import org.apache.commons.math3.linear.Array2DRowRealMatrix

class CustomKalmanFilter(
    processModel: ProcessModel,
    measurementModel: MeasurementModel
) : KalmanFilter(processModel, measurementModel)

class CustomProcessModel(
    initialStateEstimate: RealVector,
    initialErrorCovariance: RealMatrix,
    transitionMatrix: RealMatrix,
    processNoise: RealMatrix
) : ProcessModel {
    private val initialStateEstimate: RealVector = initialStateEstimate.copy()
    private val initialErrorCovariance: RealMatrix = initialErrorCovariance.copy()
    private val transitionMatrix: RealMatrix = transitionMatrix.copy()
    private val processNoise: RealMatrix = processNoise.copy()

    override fun getInitialStateEstimate(): RealVector = initialStateEstimate

    override fun getInitialErrorCovariance(): RealMatrix = initialErrorCovariance

    override fun getStateTransitionMatrix(): RealMatrix = transitionMatrix

    override fun getControlMatrix(): RealMatrix = transitionMatrix

    override fun getProcessNoise(): RealMatrix = processNoise

}

class CustomMeasurementModel(
    measurementMatrix: RealMatrix,
    measurementNoise: RealMatrix
) : DefaultMeasurementModel(measurementMatrix, measurementNoise)