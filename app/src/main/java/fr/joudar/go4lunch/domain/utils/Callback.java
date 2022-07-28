package fr.joudar.go4lunch.domain.utils;

public interface Callback<ResultsType> {
  void onSuccess(ResultsType results);
  void onFailure();
}
