package com.google.sitebricks.compiler;

import org.mvel.ErrorDetail;

/**
 * Represents a static analysis error or warning due to a
 * sitebricks static check failure.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public abstract class AnalysisError {

  public abstract CompileErrors getReason();

  public static CompileErrorBuilder in(String fragment) {
    return new Builder(fragment);
  }

  public static interface CompileErrorBuilder {
    CompileErrorBuilder near(int line);

    AnalysisError causedBy(ExpressionCompileException e);

    AnalysisError causedBy(CompileErrors reason);

    AnalysisError causedBy(CompileErrors reason, ExpressionCompileException e);

    AnalysisError causedBy(CompileErrors reason, String cause);
  }

  private static class Builder implements CompileErrorBuilder {
    private final String fragment;
    private int line;

    private Builder(String fragment) {
      this.fragment = fragment;
    }

    public CompileErrorBuilder near(int line) {
      this.line = line;

      return this;
    }

    public AnalysisError causedBy(ExpressionCompileException e) {
      System.out.println(e.getError());
      return new AnalysisErrorImpl(fragment, line, e.getError());
    }

    public AnalysisError causedBy(CompileErrors reason) {
      return new AnalysisErrorImpl(fragment, line, reason);
    }

    public AnalysisError causedBy(CompileErrors reason, ExpressionCompileException e) {
      return new AnalysisErrorImpl(fragment, line, e.getError(), reason);
    }

    public AnalysisError causedBy(CompileErrors reason, String cause) {
      return new AnalysisErrorImpl(fragment, line,
          new EvaluatorCompiler.CompileErrorDetail(cause, new ErrorDetail(cause, true)), reason);
    }
  }

  private static class AnalysisErrorImpl extends AnalysisError {
    private final String fragment;
    private final int line;
    private final EvaluatorCompiler.CompileErrorDetail error;
    private final CompileErrors reason;

    public AnalysisErrorImpl(String fragment, int line, EvaluatorCompiler.CompileErrorDetail error) {
      this.fragment = fragment;
      this.line = line;
      this.error = error;

      this.reason = CompileErrors.ILLEGAL_EXPRESSION;
    }

    public AnalysisErrorImpl(String fragment, int line, CompileErrors reason) {
      this.fragment = fragment;
      this.line = line;
      this.reason = reason;

      this.error = null;
    }

    public AnalysisErrorImpl(String fragment, int line, EvaluatorCompiler.CompileErrorDetail error,
                            CompileErrors reason) {

      this.fragment = fragment;
      this.line = line;
      this.error = error;
      this.reason = reason;
    }

    @Override
    public String toString() {
      //TODO make this nicer?
      return reason.toString();
    }

    public CompileErrors getReason() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
  }
}