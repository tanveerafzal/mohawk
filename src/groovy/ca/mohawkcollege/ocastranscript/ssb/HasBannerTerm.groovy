package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.baseline.Term

trait HasBannerTerm {
    Term term

    abstract String getTermCode()

    Term getTerm() {
        if (!this.term) {
            this.term = Term.findByStvtermCode(this.getTermCode())
        }
        return this.term
    }
}