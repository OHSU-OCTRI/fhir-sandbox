$(function () {
  const inputGroupSelector = '.input-group.date-control';
  const inputSelector = 'input[data-provide=datepicker]';
  const appendSelector = '.input-group-text';

  /**
   * Convert the date time format pattern from java to the pattern supported by
   * the datepicker library. Note that this only supports a subset of patterns.
   *
   * see: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ofPattern-java.lang.String
   * see: https://api.jqueryui.com/datepicker/#utility-formatDate
   *
   * @param {String} javaPattern
   * @returns {String} datepicker pattern
   */
  function convertDateFormat(javaPattern) {
    if (javaPattern === 'yyyy-MM-dd') {
      return 'yy-mm-dd';
    }
    if (javaPattern === 'MM-dd-yyyy') {
      return 'mm-dd-yy';
    }
    throw new Error(`Date format conversion not defined for ${javaPattern}`);
  }

  if (typeof $.fn.datepicker === 'function') {
    // attach date pickers to inputs
    $(inputSelector).each(function () {
      // default options
      let options = {
        // 'yy' is actually 4-digit year
        dateFormat: 'yy-mm-dd',
        changeMonth: true,
        changeYear: true,
        yearRange: 'c-120:c+10'
      };

      // override using data attributes
      const dateFormat = $(this).data('dateFormat');
      if (dateFormat) {
        options.dateFormat = convertDateFormat(dateFormat);
      }

      const minDate = $(this).data('minDate');
      if (minDate) {
        options.minDate = minDate;
      }

      const maxDate = $(this).data('maxDate');
      if (maxDate) {
        options.maxDate = maxDate;
      }

      $(this).datepicker(options);
    });

    // when the input group add-on is clicked, toggle the picker on the related input
    // the event is delegated to the input group div to simplify locating the input
    $(inputGroupSelector).on('click', appendSelector, function (evt) {
      evt.stopPropagation();
      let pickerElement = $(evt.delegateTarget).find(inputSelector);
      let pickerVisible = pickerElement.datepicker('widget').is(':visible');
      pickerElement.datepicker(pickerVisible ? 'hide' : 'show');
    });
  }
});
